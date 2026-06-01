package com.raghu.queue_system.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.raghu.queue_system.dto.DoctorRequestDTO;
import com.raghu.queue_system.dto.DoctorResponseDTO;
import com.raghu.queue_system.model.Doctor;
import com.raghu.queue_system.repository.DoctorRepository;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public DoctorResponseDTO createDoctor(DoctorRequestDTO request) {
        Optional<Doctor> existing = doctorRepository.findByEmail(request.getEmail());
        Doctor doctor;
        if (existing.isPresent()) {
            doctor = existing.get();
        } else {
            doctor = new Doctor();
        }
        doctor.setName(request.getName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setAvgServiceTime(request.getAvgServiceTime());
        doctor.setEmail(request.getEmail());

        Doctor saved = doctorRepository.save(doctor);
        return convertToDTO(saved);
    }

    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public DoctorResponseDTO convertToDTO(Doctor doctor) {
        return new DoctorResponseDTO(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getAvgServiceTime(),
                doctor.getEmail()
        );
    }
}