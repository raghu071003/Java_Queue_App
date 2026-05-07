package com.raghu.queue_system.repository;

import com.raghu.queue_system.model.QueueEntry;
import com.raghu.queue_system.model.QueueStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    List<QueueEntry> findByDoctorIdOrderByPosition(Long doctorId);

    List<QueueEntry> findByDoctorIdAndStatusOrderByPosition(
            Long doctorId,
            QueueStatus status);


    boolean existsByUserIdAndDoctorIdAndStatus(
        Long userId,
        Long doctorId,
        QueueStatus status
    );
}