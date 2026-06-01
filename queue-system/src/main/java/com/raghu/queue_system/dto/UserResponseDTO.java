package com.raghu.queue_system.dto;

import com.raghu.queue_system.model.UserRole;

public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private Long doctorId;

    public UserResponseDTO(Long id, String name, String email, UserRole role, Long doctorId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.doctorId = doctorId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public Long getDoctorId() {
        return doctorId;
    }
}