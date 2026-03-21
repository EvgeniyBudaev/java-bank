package com.notificationservice.repository;

import com.notificationservice.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {
}