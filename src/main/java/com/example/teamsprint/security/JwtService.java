package com.example.teamsprint.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Value("${jwt.expiration}")
    private String JWT_EXPIRATION;

    public String generateToken(UserPrincipal user) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Long.parseLong(JWT_EXPIRATION)))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }
}
