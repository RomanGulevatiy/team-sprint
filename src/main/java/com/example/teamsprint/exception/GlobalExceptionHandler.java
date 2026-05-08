package com.example.teamsprint.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(EntityNotFoundException exception) {
        return ErrorResponse.builder()
                .error("Entity Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .message(exception.getMessage())
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(UserNotInProjectException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUserNotInProjectException(UserNotInProjectException exception) {
        return ErrorResponse.builder()
                .error("User Not In Project")
                .status(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
}
