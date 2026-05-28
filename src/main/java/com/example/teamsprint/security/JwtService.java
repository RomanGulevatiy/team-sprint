package com.example.teamsprint.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.access-expiration}")
    private String JWT_ACCESS_EXPIRATION;

    @Value("${jwt.refresh-expiration}")
    private String JWT_REFRESH_EXPIRATION;

    public String generateAccessToken(UserPrincipal user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getUsername(), Long.parseLong(JWT_ACCESS_EXPIRATION));
    }

    public String generateRefreshToken(UserPrincipal user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getUsername(), Long.parseLong(JWT_REFRESH_EXPIRATION));
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationMillis) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    public String extractEmail(String token) throws JwtException {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserPrincipal userPrincipal) {
        final String email = extractEmail(token);
        return (email.equals(userPrincipal.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
