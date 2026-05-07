package com.raghu.queue_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.raghu.queue_system.dto.ErrorResponse;

import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex) {
        return new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDoctorNotFound(DoctorNotFoundException ex) {
        return new ErrorResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return new ErrorResponse(
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                errors);
    }

    @ExceptionHandler(AlreadyInQueueException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyInQueue(
            AlreadyInQueueException ex) {

        return new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value());
    }
}