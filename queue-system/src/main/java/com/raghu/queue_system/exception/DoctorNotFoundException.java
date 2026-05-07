package com.raghu.queue_system.exception;

public class DoctorNotFoundException extends RuntimeException  {
    public DoctorNotFoundException(String message){
        super(message);
    }
}
