package com.example.teamsprint.scheduled;

import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.VerificationToken;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnverifiedAccountCleanup {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredUnverifiedAccounts() {
        List<VerificationToken> expiredTokens = verificationTokenRepository.findAllExpired(LocalDateTime.now());

        List<User> usersToDelete = expiredTokens.stream()
                .map(VerificationToken::getUser)
                .filter(user -> !user.isEnabled())
                .toList();

        if(!usersToDelete.isEmpty()) {
            userRepository.deleteAll(usersToDelete);
            log.info("Cleaned up {} unverified expired accounts", usersToDelete.size());
        }
    }
}
