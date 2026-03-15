package com.accountsservice.service;

import com.accountsservice.controller.dto.request.RequestAccountCreateDto;
import com.accountsservice.controller.dto.response.ResponseAccountDto;
import com.accountsservice.entity.AccountEntity;
import com.accountsservice.repository.AccountRepository;
import com.accountsservice.service.mapper.AccountMapper;
import com.accountsservice.shared.exception.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
    public ResponseAccountDto createAccount(RequestAccountCreateDto dto) {
        if (accountRepository.existsByUserId(dto.userId())) {
            throw new IllegalArgumentException("Account already exists: " + dto.userId());
        }

        AccountEntity accountEntity = accountMapper.toCreateAccountEntity(dto);
        AccountEntity accountSaved = accountRepository.save(accountEntity);
        return accountMapper.toResponseAccountDto(accountSaved);
    }

    /**
     * Получить аккаунт по user_id пользователя.===================
     */
    public ResponseAccountDto getAccountByUserId(UUID userId) {
        AccountEntity account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found", "User with id '" + userId + "' does not exist"));
        return accountMapper.toResponseAccountDto(account);
    }
}
