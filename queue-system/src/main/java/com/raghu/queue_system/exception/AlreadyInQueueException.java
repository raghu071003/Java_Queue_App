package com.raghu.queue_system.exception;

public class AlreadyInQueueException extends RuntimeException {

    public AlreadyInQueueException(String message) {
        super(message);
    }
}