package com.example.teamsprint.service.impl;

import com.example.teamsprint.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendVerificationEmail(String to, String username, String token) {
        String link = "http://localhost:8080/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Verify your email - Team Sprint");
        message.setText(buildVerificationEmailText(username, link));

        mailSender.send(message);
    }

    private String buildVerificationEmailText(String username, String code) {
        return String.format("""
                Hello %1$s!
                
                To activate your account, please use the following verification link:
                %2$s
                
                This link is valid for 24 hours.
                
                If you didn't create an account, please ignore this email.
                
                Best regards,
                Team Sprint
                """, username, code);
    }
}
