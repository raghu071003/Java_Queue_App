package com.raghu.queue_system.service;

import com.raghu.queue_system.dto.QueuePatientDTO;
import com.raghu.queue_system.dto.QueueResponseDTO;
import com.raghu.queue_system.dto.QueueEntryDTO;
import com.raghu.queue_system.model.*;
import com.raghu.queue_system.repository.*;
import com.raghu.queue_system.exception.*;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Set;

@Service
public class QueueService {

        private final QueueEntryRepository queueEntryRepository;
        private final UserRepository userRepository;
        private final DoctorRepository doctorRepository;
        private final RedisTemplate<String, Object> redisTemplate;
        private final SimpMessagingTemplate messagingTemplate;

        public QueueService(
                        QueueEntryRepository queueEntryRepository,
                        UserRepository userRepository,
                        DoctorRepository doctorRepository,
                        RedisTemplate<String, Object> redisTemplate,
                        SimpMessagingTemplate messagingTemplate) {

                this.queueEntryRepository = queueEntryRepository;
                this.userRepository = userRepository;
                this.doctorRepository = doctorRepository;
                this.redisTemplate = redisTemplate;
                this.messagingTemplate = messagingTemplate;
        }

        public QueueResponseDTO getQueueDetails(Long queueEntryId) {
                QueueEntry entry = queueEntryRepository.findById(queueEntryId)
                                .orElseThrow(() -> new QueueEntryNotFoundException("Queue entry not found!"));

                int peopleAhead = entry.getPosition() - 1;
                int estimatedWait = peopleAhead * entry.getDoctor().getAvgServiceTime();

                return new QueueResponseDTO(entry.getUser().getName(), entry.getDoctor().getName(), entry.getPosition(),
                                estimatedWait, entry.getStatus());
        }

        public QueueEntryDTO joinQueueForAuthenticatedUser(
                        String email,
                        Long doctorId) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserNotFoundException("User Not Found!"));

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

                List<QueueEntry> queue = queueEntryRepository.findByDoctorIdAndStatusOrderByPosition(doctorId,
                                QueueStatus.WAITING);
                int nextPosition = queue.size() + 1;

                QueueEntry entry = new QueueEntry();

                entry.setUser(user);
                entry.setDoctor(doctor);
                entry.setPosition(nextPosition);
                entry.setStatus(QueueStatus.WAITING);
                entry.setJoinedAt(java.time.LocalDateTime.now());

                String redisKey = "doctor_queue:" + doctorId;

                redisTemplate.opsForZSet().add(
                                redisKey,
                                user.getEmail(),
                                nextPosition);
                messagingTemplate.convertAndSend(
                                "/topic/queue/" + doctorId,
                                getRedisQueue(doctorId));
                QueueEntry saved = queueEntryRepository.save(entry);
                return convertToDTO(saved);
        }

        public List<QueuePatientDTO> getDoctorQueue(Long doctorId) {

                List<QueueEntry> queue = queueEntryRepository
                                .findByDoctorIdAndStatusOrderByPosition(
                                                doctorId,
                                                QueueStatus.WAITING);

                return queue.stream()
                                .map(entry -> new QueuePatientDTO(
                                                entry.getUser().getName(),
                                                entry.getPosition(),
                                                entry.getStatus()))
                                .toList();
        }

        public Set<Object> getRedisQueue(Long doctorId) {

                String redisKey = "doctor_queue:" + doctorId;

                return redisTemplate.opsForZSet()
                                .range(redisKey, 0, -1);
        }

        public void removeFromRedisQueue(
                        Long doctorId,
                        String email) {

                String redisKey = "doctor_queue:" + doctorId;

                redisTemplate.opsForZSet()
                                .remove(redisKey, email);
        }

        public Object getCurrentPatient(Long doctorId) {

                String redisKey = "doctor_queue:" + doctorId;

                Set<Object> result = redisTemplate.opsForZSet()
                                .range(redisKey, 0, 0);

                if (result == null || result.isEmpty()) {
                        return "No patients waiting";
                }

                return result.iterator().next();
        }

        private void shiftPositions(Long doctorId, int fromPosition) {
                List<QueueEntry> subsequentEntries = queueEntryRepository
                                .findByDoctorIdAndStatusOrderByPosition(doctorId, QueueStatus.WAITING);

                String redisKey = "doctor_queue:" + doctorId;

                for (QueueEntry entry : subsequentEntries) {
                        if (entry.getPosition() != null && entry.getPosition() > fromPosition) {
                                int newPosition = entry.getPosition() - 1;
                                entry.setPosition(newPosition);
                                queueEntryRepository.save(entry);

                                // Update Redis score for the waiting user
                                redisTemplate.opsForZSet().add(
                                                redisKey,
                                                entry.getUser().getEmail(),
                                                newPosition);
                        }
                }
        }

        public QueueEntryDTO startNextConsultation(Long doctorId) {

                List<QueueEntry> queue = queueEntryRepository
                                .findByDoctorIdAndStatusOrderByPosition(
                                                doctorId,
                                                QueueStatus.WAITING);

                if (queue.isEmpty()) {
                        throw new QueueEmptyException("Queue is empty");
                }

                QueueEntry currentPatient = queue.get(0);
                Integer oldPosition = currentPatient.getPosition();

                currentPatient.setStatus(QueueStatus.IN_PROGRESS);
                currentPatient.setPosition(null);
                QueueEntry saved = queueEntryRepository.save(currentPatient);

                // Remove from Redis waiting queue
                removeFromRedisQueue(doctorId, currentPatient.getUser().getEmail());

                // Shift remaining waiting patients
                if (oldPosition != null) {
                        shiftPositions(doctorId, oldPosition);
                }

                // Broadcast updated queue to WebSockets
                messagingTemplate.convertAndSend(
                                "/topic/queue/" + doctorId,
                                getRedisQueue(doctorId));

                return convertToDTO(saved);
        }

        public void completeConsultation(Long queueEntryId) {
                QueueEntry current = queueEntryRepository.findById(queueEntryId)
                                .orElseThrow(() -> new QueueEntryNotFoundException("Queue entry not found"));

                QueueStatus oldStatus = current.getStatus();
                Integer oldPosition = current.getPosition();

                current.setStatus(QueueStatus.DONE);
                current.setPosition(null);
                queueEntryRepository.save(current);

                Long doctorId = current.getDoctor().getId();
                String email = current.getUser().getEmail();

                // Remove from Redis queue
                removeFromRedisQueue(doctorId, email);

                // If they were WAITING and had a valid position, shift subsequent patients
                if (oldStatus == QueueStatus.WAITING && oldPosition != null) {
                        shiftPositions(doctorId, oldPosition);
                }

                // Broadcast updated queue to WebSockets after removals/updates are completed
                messagingTemplate.convertAndSend(
                                "/topic/queue/" + doctorId,
                                getRedisQueue(doctorId));
        }

        public QueueResponseDTO getMyQueueDetails(
                        String email,
                        Long doctorId) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserNotFoundException("User not found"));

                QueueEntry entry;
                if (doctorId != null) {
                        entry = queueEntryRepository
                                        .findActiveQueueEntry(doctorId, user.getId())
                                        .orElseThrow(() -> new QueueEntryNotFoundException("Not in queue"));
                } else {
                        List<QueueEntry> activeEntries = queueEntryRepository.findActiveQueueEntries(user.getId());
                        if (activeEntries.isEmpty()) {
                                throw new QueueEntryNotFoundException("Not in queue");
                        }
                        entry = activeEntries.get(0);
                }

                Integer positionVal = entry.getPosition();
                int position = positionVal != null ? positionVal : 0;
                int estimatedWait = position > 0 ? (position - 1) * entry.getDoctor().getAvgServiceTime() : 0;

                return new QueueResponseDTO(
                                user.getName(),
                                entry.getDoctor().getName(),
                                position,
                                estimatedWait,
                                entry.getStatus());
        }

        public QueueEntryDTO getActiveConsultation(Long doctorId) {
                List<QueueEntry> active = queueEntryRepository
                                .findByDoctorIdAndStatusOrderByPosition(doctorId, QueueStatus.IN_PROGRESS);
                if (active.isEmpty()) {
                        return null;
                }
                return convertToDTO(active.get(0));
        }

        public QueueEntryDTO convertToDTO(QueueEntry entry) {
                return new QueueEntryDTO(
                                entry.getId(),
                                entry.getUser().getId(),
                                entry.getUser().getName(),
                                entry.getUser().getEmail(),
                                entry.getDoctor().getId(),
                                entry.getDoctor().getName(),
                                entry.getPosition(),
                                entry.getStatus(),
                                entry.getJoinedAt());
        }
}