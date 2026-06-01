package com.raghu.queue_system.controller;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raghu.queue_system.dto.ApiResponse;
import com.raghu.queue_system.dto.QueueEntryDTO;
import com.raghu.queue_system.dto.QueuePatientDTO;
import com.raghu.queue_system.dto.QueueResponseDTO;
import com.raghu.queue_system.service.QueueService;

@RestController
@RequestMapping("/queue")
public class QueueController {
    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/join")
    public ApiResponse<QueueEntryDTO> joinQueue(Authentication authentication, @RequestParam Long doctorId) {
        String email = authentication.getName();
        QueueEntryDTO response = queueService.joinQueueForAuthenticatedUser(email, doctorId);
        return ApiResponse.success("Joined queue successfully", response);
    }

    @GetMapping("/details/{id}")
    public ApiResponse<QueueResponseDTO> getQueueDetails(@PathVariable Long id) {
        QueueResponseDTO response = queueService.getQueueDetails(id);
        return ApiResponse.success("Queue details retrieved successfully", response);
    }

    @GetMapping("/doctor/{doctorId}")
    public ApiResponse<List<QueuePatientDTO>> getDoctorQueue(@PathVariable Long doctorId) {
        List<QueuePatientDTO> response = queueService.getDoctorQueue(doctorId);
        return ApiResponse.success("Doctor queue retrieved successfully", response);
    }

    @GetMapping("/redis/{doctorId}")
    public ApiResponse<Set<Object>> getDocQueue(@PathVariable Long doctorId) {
        Set<Object> response = queueService.getRedisQueue(doctorId);
        return ApiResponse.success("Redis queue retrieved successfully", response);
    }

    @GetMapping("/current/{doctorId}")
    public ApiResponse<Object> getPositionForDoc(@PathVariable Long doctorId) {
        Object response = queueService.getCurrentPatient(doctorId);
        return ApiResponse.success("Current patient retrieved successfully", response);
    }

    @PostMapping("/start")
    public ApiResponse<QueueEntryDTO> startConsultation(@RequestParam Long doctorId) {
        QueueEntryDTO response = queueService.startNextConsultation(doctorId);
        return ApiResponse.success("Consultation started successfully", response);
    }

    @PostMapping("/complete/{queueEntryId}")
    public ApiResponse<String> completeConsultation(@PathVariable Long queueEntryId) {
        queueService.completeConsultation(queueEntryId);
        return ApiResponse.success("Consultation completed", "Consultation completed");
    }

    @GetMapping("/active/{doctorId}")
    public ApiResponse<QueueEntryDTO> getActiveConsultation(@PathVariable Long doctorId) {
        QueueEntryDTO response = queueService.getActiveConsultation(doctorId);
        return ApiResponse.success("Active consultation retrieved successfully", response);
    }

    @GetMapping("/my-position")
    public ApiResponse<QueueResponseDTO> getMyPosition(Authentication authentication, @RequestParam(required = false) Long doctorId) {
        QueueResponseDTO response = queueService.getMyQueueDetails(authentication.getName(), doctorId);
        return ApiResponse.success("My queue details retrieved successfully", response);
    }
}
