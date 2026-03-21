package com.notificationservice.repository;

import com.notificationservice.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {
    @Modifying
    @Transactional
    @Query("DELETE FROM IdempotencyKeyEntity k WHERE k.createdAt < :threshold")
    int deleteByCreatedAtBefore(LocalDateTime threshold);
}