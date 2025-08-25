package org.heigvd.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;
import java.util.UUID;

@ApplicationScoped
/**
 * Service utilitaire pour la génération de jetons JWT.
 */
public class JwtService {

    /**
     * Génère un jeton JWT signé pour un utilisateur.
     *
     * @param userId identifiant de l'utilisateur
     * @return jeton JWT signé
     */
    public String generateToken(UUID userId) {
        return Jwt
                .issuer("ferum")
                .upn(userId.toString())
                .subject(userId.toString())
                .groups(Set.of("user"))
                .sign();
    }
}
