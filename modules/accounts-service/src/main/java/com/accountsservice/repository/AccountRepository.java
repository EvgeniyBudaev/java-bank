package com.accountsservice.repository;

import com.accountsservice.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    /**
     * Найти аккаунт по user_id пользователя.
     */
    Optional<AccountEntity> findByUserId(UUID uuid);

    /**
     * Проверить существование аккаунта по user_id пользователя.
     */
    boolean existsByUserId(UUID uuid);
}
