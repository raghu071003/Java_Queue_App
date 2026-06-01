package com.raghu.queue_system.dto;

public class DoctorResponseDTO {
    private Long id;
    private String name;
    private String specialization;
    private Integer avgServiceTime;
    private String email;

    public DoctorResponseDTO(Long id, String name, String specialization, Integer avgServiceTime, String email) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.avgServiceTime = avgServiceTime;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getAvgServiceTime() {
        return avgServiceTime;
    }

    public void setAvgServiceTime(Integer avgServiceTime) {
        this.avgServiceTime = avgServiceTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
