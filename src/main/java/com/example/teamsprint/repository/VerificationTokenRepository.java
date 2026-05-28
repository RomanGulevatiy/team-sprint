package com.example.teamsprint.repository;

import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    @Query("SELECT v FROM VerificationToken v WHERE v.expiresAt < :now")
    List<VerificationToken> findAllExpired(@Param("now") LocalDateTime now);
}
