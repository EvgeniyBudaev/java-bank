package com.cashservice.repository;

import com.cashservice.entity.CashOutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<CashOutboxEntity, Long> {
    // Выбираем только необработанные записи, отсортированные по дате создания
    List<CashOutboxEntity> findByProcessedFalseOrderByCreatedAtAsc(Pageable pageable);

    // Удаляем по ID
    void deleteById(Long id);
}
