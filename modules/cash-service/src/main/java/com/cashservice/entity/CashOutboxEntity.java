package com.cashservice.entity;

import com.cashservice.service.NotificationType;
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
 * Модель для таблицы cash_outbox.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cash_outbox")
public class CashOutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cash_outbox_id_seq")
    @SequenceGenerator(name = "cash_outbox_id_seq", sequenceName = "cash_outbox_id_seq", allocationSize = 1)
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
