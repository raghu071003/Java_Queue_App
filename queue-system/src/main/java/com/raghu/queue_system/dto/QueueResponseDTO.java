package com.raghu.queue_system.dto;

import com.raghu.queue_system.model.QueueStatus;

public class QueueResponseDTO {

    private String patientName;
    private String doctorName;
    private Integer position;
    private Integer estimatedWaitTime;
    private QueueStatus status;

    public QueueResponseDTO(String patientName,
                            String doctorName,
                            Integer position,
                            Integer estimatedWaitTime,
                            QueueStatus status) {

        this.patientName = patientName;
        this.doctorName = doctorName;
        this.position = position;
        this.estimatedWaitTime = estimatedWaitTime;
        this.status = status;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Integer getPosition() {
        return position;
    }

    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

public QueueStatus getStatus() {
        return status;
    }
}