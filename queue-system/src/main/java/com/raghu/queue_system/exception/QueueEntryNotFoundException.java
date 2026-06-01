package com.raghu.queue_system.exception;

public class QueueEntryNotFoundException extends RuntimeException {
    public QueueEntryNotFoundException(String message) {
        super(message);
    }
}
