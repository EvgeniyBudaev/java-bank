package com.accountsservice.service;

import com.accountsservice.controller.dto.response.ResponseAccountDto;
import com.accountsservice.entity.AccountEntity;
import com.accountsservice.entity.OutboxEntity;
import com.accountsservice.repository.AccountRepository;
import com.accountsservice.repository.OutboxRepository;
import com.accountsservice.service.mapper.AccountMapper;
import com.accountsservice.exception.InsufficientFundsException;
import com.accountsservice.exception.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для работы с аккаунтами.
 */
@Service
@AllArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final OutboxRepository outboxRepository;

    /**
     * Создать новый аккаунт.
     */
    @Override
    @Transactional
    public ResponseAccountDto createAccount(UUID userId) {
        if (accountRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Account already exists: " + userId);
        }

        AccountEntity accountEntity = accountMapper.toCreateAccountEntity(userId);
        AccountEntity accountSaved = accountRepository.save(accountEntity);

        return accountMapper.toResponseAccountDto(accountSaved);
    }

    /**
     * Получить аккаунт по user_id пользователя.
     */
    @Override
    public ResponseAccountDto getAccountByUserId(UUID userId) {
        AccountEntity account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User not found", "User with id '" + userId + "' does not exist"));

        return accountMapper.toResponseAccountDto(account);
    }

    /**
     * Получить все аккаунты (для выбора получателя перевода).
     */
    @Override
    public List<ResponseAccountDto> getAllAccounts(UUID excludeUuid) {
        return accountRepository.findAll().stream()
                .filter(account -> !account.getUserId().equals(excludeUuid))
                .map(accountMapper::toResponseAccountDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResponseAccountDto deposit(UUID userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        AccountEntity account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User not found", "User with id '" + userId + "' does not exist"));

        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(Instant.now());
        AccountEntity accountSaved = accountRepository.save(account);

        ResponseAccountDto responseAccountDto = accountMapper.toResponseAccountDto(accountSaved);

        // Сохраняем событие в Outbox таблицу
        saveToOutbox(
                NotificationType.CASH_IN,
                amount,
                userId,
                Instant.now()
        );

        return responseAccountDto;
    }

    @Override
    @Transactional
    public ResponseAccountDto withdraw(UUID userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        AccountEntity account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "User not found", "User with id '" + userId + "' does not exist"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds",
                    "Account balance (" + account.getBalance() + ") is less than withdrawal amount (" + amount + ")"
            );
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(Instant.now());
        AccountEntity accountSaved = accountRepository.save(account);

        ResponseAccountDto responseAccountDto = accountMapper.toResponseAccountDto(accountSaved);

        // Сохраняем событие в Outbox таблицу
        saveToOutbox(
                NotificationType.CASH_OUT,
                amount,
                userId,
                Instant.now()
        );

        return responseAccountDto;
    }

    @Override
    @Transactional
    public ResponseAccountDto refund(UUID authenticatedUserId, UUID targetUserId, BigDecimal amount) {
        // 1. Валидация суммы
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // 2. Проверка существования счета получателя
        AccountEntity account = accountRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new NotFoundException(
                        "User not found",
                        "User with id '" + targetUserId + "' does not exist"
                ));

        // 3. Проверка безопасности (Важно для компенсации)
        // Refund — это привилегированная операция. Обычно она вызывается сервисом, а не пользователем.
        // Если authenticatedUserId != targetUserId, убедитесь, что у вызывающего есть права.
        // В микросервисной архитектуре это часто проверяется через Scope в SecurityConfig,
        // но здесь добавим базовую проверку на уровне сервиса.
        if (!authenticatedUserId.equals(targetUserId)) {
            log.warn("Refund initiated by {} for user {}", authenticatedUserId, targetUserId);
            // Здесь можно добавить явную проверку роли, если требуется
            // if (!isAdmin(authenticatedUserId)) throw new AccessDeniedException(...);
        }

        // 4. Зачисление средств (компенсация)
        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(java.time.Instant.now());

        AccountEntity accountSaved = accountRepository.save(account);

        log.info("Refund successful: userId={}, amount={}", targetUserId, amount);
        return accountMapper.toResponseAccountDto(accountSaved);
    }

    /**
     * Сохраняет событие в таблицу Outbox.
     * Вызывается внутри @Transactional метода, поэтому сохранение происходит
     * в той же транзакции, что и обновление баланса.
     */
    private void saveToOutbox(
            NotificationType type,
            BigDecimal amount,
            UUID userId,
            Instant occurredAt
    ) {
        OutboxEntity outbox = new OutboxEntity();
        outbox.setType(type);
        outbox.setUserId(userId);
        outbox.setAmount(amount);
        outbox.setCreatedAt(occurredAt);
        outbox.setProcessed(false);
        outboxRepository.save(outbox);
        log.info("Saved outbox event: type={}, userId={}, amount={}", type, userId, amount);
    }
}
