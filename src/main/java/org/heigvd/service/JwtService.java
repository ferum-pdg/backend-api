package org.heigvd.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * Utility service for JWT token generation.
 */
@ApplicationScoped
public class JwtService {

    /**
     * Generates a signed JWT token for a user.
     *
     * @param userId user identifier
     * @return signed JWT token
     */
    public String generateToken(UUID userId) {
        return Jwt
                .issuer("ferum")
                .upn(userId.toString())
                .subject(userId.toString())
                .groups(Set.of("user"))
                .expiresIn(Duration.ofDays(30))
                .sign();
    }
}