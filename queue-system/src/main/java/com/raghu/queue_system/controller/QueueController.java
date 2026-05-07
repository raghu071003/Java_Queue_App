package com.raghu.queue_system.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raghu.queue_system.dto.QueuePatientDTO;
import com.raghu.queue_system.dto.QueueResponseDTO;
import com.raghu.queue_system.model.QueueEntry;
import com.raghu.queue_system.service.QueueService;

@RestController
@RequestMapping("/queue")
public class QueueController {
    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/join")
    public QueueEntry joinQueue(Authentication authentication,
            @RequestParam Long doctorId) {

        String email = authentication.getName();

        return queueService
                .joinQueueForAuthenticatedUser(email, doctorId);
    }

    @GetMapping("/details/{id}")
    public QueueResponseDTO getQueueDetails(@PathVariable Long id) {
        return queueService.getQueueDetails(id);
    }

    @PostMapping("/next")
    public String serveNextPatient(@RequestParam Long doctorId) {
        queueService.serveNextPatient(doctorId);
        return "Next Patient Served";
    }

    @GetMapping("/doctor/{doctorId}")
    public List<QueuePatientDTO> getDoctorQueue(
            @PathVariable Long doctorId) {

        return queueService.getDoctorQueue(doctorId);
    }
}
