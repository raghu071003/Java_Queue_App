package com.raghu.queue_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.raghu.queue_system.dto.ApiResponse;
import com.raghu.queue_system.dto.DoctorRequestDTO;
import com.raghu.queue_system.dto.DoctorResponseDTO;
import com.raghu.queue_system.service.DoctorService;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ApiResponse<DoctorResponseDTO> createDoctor(@RequestBody DoctorRequestDTO doctor) {
        DoctorResponseDTO response = doctorService.createDoctor(doctor);
        return ApiResponse.success("Doctor created successfully", response);
    }

    @GetMapping
    public ApiResponse<List<DoctorResponseDTO>> getAllDoctors() {
        List<DoctorResponseDTO> response = doctorService.getAllDoctors();
        return ApiResponse.success("Doctors retrieved successfully", response);
    }
}