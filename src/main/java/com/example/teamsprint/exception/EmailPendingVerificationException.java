package com.example.teamsprint.exception;

public class EmailPendingVerificationException extends RuntimeException {
    public EmailPendingVerificationException(String message) {
        super(message);
    }
}
