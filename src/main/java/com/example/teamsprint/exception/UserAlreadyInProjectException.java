package com.example.teamsprint.exception;

public class UserAlreadyInProjectException extends RuntimeException {
    public UserAlreadyInProjectException(String message) {
        super(message);
    }
}

