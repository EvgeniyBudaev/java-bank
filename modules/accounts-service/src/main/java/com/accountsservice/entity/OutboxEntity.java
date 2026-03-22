package com.accountsservice.entity;

import com.accountsservice.service.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Модель для таблицы accounts_outbox.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts_outbox")
public class OutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounts_outbox_id_seq")
    @SequenceGenerator(name = "accounts_outbox_id_seq", sequenceName = "accounts_outbox_id_seq", allocationSize = 1)
    private Long id;

    @NotNull(message = "type cannot be null")
    @Column(name = "type", nullable = false, updatable = false)
    private NotificationType type;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Min(value = 0, message = "Balance cannot be negative")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Поле для отслеживания статуса обработки
    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
