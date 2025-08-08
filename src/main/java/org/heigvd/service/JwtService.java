package org.heigvd.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.Account;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    public String generateToken(Account account) {
        Set<String> groups = new HashSet<>();
        groups.add("user");

        return Jwt.issuer("ferum")
                .upn(account.getEmail())
                .groups(groups)
                .claim("userId", account.getId().toString())
                .claim("firstName", account.getFirstName())
                .claim("lastName", account.getLastName())
                .expiresIn(Duration.ofHours(24))
                .sign();
    }
}