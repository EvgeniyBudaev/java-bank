package com.accountsservice.repository;

import com.accountsservice.entity.OutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntity, Long> {
    // Выбираем только необработанные записи, отсортированные по дате создания
    List<OutboxEntity> findByProcessedFalseOrderByCreatedAtAsc(Pageable pageable);

    // Удаляем по ID
    void deleteById(Long id);
}
