package com.example.teamsprint.service;

public interface EmailService {

    void sendVerificationEmail(String to, String username, String token);
}
