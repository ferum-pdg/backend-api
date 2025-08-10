package org.heigvd.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class JwtService {

    public String generateToken(UUID userId) {
        return Jwt
                .issuer("ferum")
                .upn(userId.toString())
                .subject(userId.toString())
                .groups(Set.of("user"))
                .sign();
    }
}
