package com.raghu.queue_system.model;

import jakarta.persistence.*;

@Entity
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String specialization;

    private Integer avgServiceTime;

    // getters & setters

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getSpecialization() {
        return this.specialization;
    }
    public Integer getAvgServiceTime() {
        return this.avgServiceTime;
    }
}