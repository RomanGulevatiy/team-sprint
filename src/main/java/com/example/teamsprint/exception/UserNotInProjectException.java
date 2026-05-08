package com.example.teamsprint.exception;

public class UserNotInProjectException extends RuntimeException {
    public UserNotInProjectException(String message) {
        super(message);
    }
}
