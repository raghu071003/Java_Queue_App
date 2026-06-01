package com.raghu.queue_system.repository;

import com.raghu.queue_system.model.QueueEntry;
import com.raghu.queue_system.model.QueueStatus;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {

    List<QueueEntry> findByDoctorIdOrderByPosition(Long doctorId);

    List<QueueEntry> findByDoctorIdAndStatusOrderByPosition(
            Long doctorId,
            QueueStatus status);

    boolean existsByUserIdAndDoctorIdAndStatus(
            Long userId,
            Long doctorId,
            QueueStatus status);

    Optional<QueueEntry> findByUserIdAndDoctorIdAndStatus(
            Long userId,
            Long doctorId,
            QueueStatus status);

    @Query("""
       SELECT q FROM QueueEntry q
       WHERE q.doctor.id = :doctorId
       AND q.user.id = :userId
       AND (q.status = com.raghu.queue_system.model.QueueStatus.WAITING OR q.status = com.raghu.queue_system.model.QueueStatus.IN_PROGRESS)
       """)
    Optional<QueueEntry> findActiveQueueEntry(
            @Param("doctorId") Long doctorId,
            @Param("userId") Long userId);

    @Query("""
       SELECT q FROM QueueEntry q
       WHERE q.user.id = :userId
       AND (q.status = com.raghu.queue_system.model.QueueStatus.WAITING OR q.status = com.raghu.queue_system.model.QueueStatus.IN_PROGRESS)
       """)
    List<QueueEntry> findActiveQueueEntries(
            @Param("userId") Long userId);

    @Query("""
       SELECT q.position
       FROM QueueEntry q
       WHERE q.doctor.id = :doctorId
       AND q.user.id = :userId
       """)
Optional<Long> getUserPosition(
        @Param("doctorId") Long doctorId,
        @Param("userId") Long userId);
}