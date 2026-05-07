package com.raghu.queue_system.dto;

import com.raghu.queue_system.model.QueueStatus;

public class QueuePatientDTO {

    private String patientName;
    private Integer position;
    private QueueStatus status;

    public QueuePatientDTO(String patientName,
            Integer position,
            QueueStatus status) {

        this.patientName = patientName;
        this.position = position;
        this.status = status;
    }

    public String getPatientName() {
        return patientName;
    }

    public Integer getPosition() {
        return position;
    }

    public QueueStatus getStatus() {
    return status;
    }
}

