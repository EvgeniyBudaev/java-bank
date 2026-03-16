package com.accountsservice.service;

import com.accountsservice.controller.dto.response.ResponseAccountDto;
import com.accountsservice.entity.AccountEntity;
import com.accountsservice.repository.AccountRepository;
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

        return accountMapper.toResponseAccountDto(accountSaved);
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

        return accountMapper.toResponseAccountDto(accountSaved);
    }
}
