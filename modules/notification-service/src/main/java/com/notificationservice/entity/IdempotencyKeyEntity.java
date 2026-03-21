package com.notificationservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_idempotency_keys")
@Getter
@NoArgsConstructor
public class IdempotencyKeyEntity {
    @Id
    private String idempotencyKey;
    private LocalDateTime createdAt;

    public IdempotencyKeyEntity(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
        this.createdAt = LocalDateTime.now();
    }
}