package com.raghu.queue_system.service;

import com.raghu.queue_system.dto.QueuePatientDTO;
import com.raghu.queue_system.dto.QueueResponseDTO;
import com.raghu.queue_system.model.*;
import com.raghu.queue_system.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;

import com.raghu.queue_system.exception.AlreadyInQueueException;
import com.raghu.queue_system.exception.DoctorNotFoundException;
import com.raghu.queue_system.exception.UserNotFoundException;;

@Service
public class QueueService {

    private final QueueEntryRepository queueEntryRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    public QueueService(QueueEntryRepository queueEntryRepository,
            UserRepository userRepository,
            DoctorRepository doctorRepository) {

        this.queueEntryRepository = queueEntryRepository;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
    }

    public QueueEntry joinQueue(Long userId, Long doctorId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        boolean alreadyWaiting = queueEntryRepository
                .existsByUserIdAndDoctorIdAndStatus(
                        userId,
                        doctorId,
                        QueueStatus.WAITING);

        if (alreadyWaiting) {
            throw new AlreadyInQueueException("User Aldready Exists!");
        }

        List<QueueEntry> queue = queueEntryRepository.findByDoctorIdOrderByPosition(doctorId);

        int nextPosition = queue.size() + 1;

        QueueEntry entry = new QueueEntry();

        entry.setUser(user);
        entry.setDoctor(doctor);
        entry.setPosition(nextPosition);
        entry.setStatus(QueueStatus.WAITING);
        entry.setJoinedAt(LocalDateTime.now());

        return queueEntryRepository.save(entry);
    }

    public QueueResponseDTO getQueueDetails(Long queueEntryId) {
        QueueEntry entry = queueEntryRepository.findById(queueEntryId)
                .orElseThrow(() -> new RuntimeException("Queue entry not found!"));

        int peopleAhead = entry.getPosition() - 1;
        int estimatedWait = peopleAhead * entry.getDoctor().getAvgServiceTime();

        return new QueueResponseDTO(entry.getUser().getName(), entry.getDoctor().getName(), entry.getPosition(),
                estimatedWait, entry.getStatus());
    }

    public void serveNextPatient(Long doctorId) {
List<QueueEntry> queue = queueEntryRepository.findByDoctorIdAndStatusOrderByPosition(doctorId, QueueStatus.WAITING);
        if (queue.isEmpty()) {
            throw new RuntimeException("Queue is empty");
        }
        QueueEntry currentPatient = queue.get(0);
        currentPatient.setStatus(QueueStatus.DONE);
        queueEntryRepository.save(currentPatient);

        for (int i = 1; i < queue.size(); i++) {
            QueueEntry entry = queue.get(i);
            entry.setPosition(entry.getPosition() - 1);
            queueEntryRepository.save(entry);
        }
    }

    public QueueEntry joinQueueForAuthenticatedUser(
            String email,
            Long doctorId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User Not Found!"));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor Not Found!"));

        boolean alreadyWaiting = queueEntryRepository
                .existsByUserIdAndDoctorIdAndStatus(
                        user.getId(),
                        doctorId,
                        QueueStatus.WAITING);

        if (alreadyWaiting) {
            throw new AlreadyInQueueException(
                    "User already exists in queue");
        }

        List<QueueEntry> queue = queueEntryRepository.findByDoctorIdAndStatusOrderByPosition(doctorId, QueueStatus.WAITING);
        int nextPosition = queue.size() + 1;

        QueueEntry entry = new QueueEntry();

        entry.setUser(user);
        entry.setDoctor(doctor);
        entry.setPosition(nextPosition);
        entry.setStatus(QueueStatus.WAITING);
        entry.setJoinedAt(java.time.LocalDateTime.now());

        return queueEntryRepository.save(entry);
    }
    public List<QueuePatientDTO> getDoctorQueue(Long doctorId) {

    List<QueueEntry> queue =
            queueEntryRepository
                    .findByDoctorIdAndStatusOrderByPosition(
                            doctorId,
                            QueueStatus.WAITING
                    );

    return queue.stream()
            .map(entry -> new QueuePatientDTO(
                    entry.getUser().getName(),
                    entry.getPosition(),
                    entry.getStatus()
            ))
            .toList();
}
}