package org.heigvd.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
                title = "API Ferum — Authentification, Workouts et Plans d'entraînement",
                version = "1.0.0",
                description = """
            Cette API permet la gestion des séances d'entraînement (workouts), des plans d'entraînement, 
            ainsi que l'authentification et le profil utilisateur.
            
            ## Fonctionnalités principales :
            - Authentification (JWT) et gestion du profil
            - Gestion des workouts (création, consultation, suppression)
            - Gestion des plans d'entraînement (création et consultation)
            
            ## Authentification
            L'API utilise un jeton JWT (schéma bearer). Ajoutez `Authorization: Bearer <token>` à vos requêtes.
            """,
                contact = @Contact(
                        name = "Équipe Ferum",
                        email = "support@ferum.app",
                        url = "https://ferum.app/support"
                ),
                license = @License(
                        name = "MIT",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Serveur de développement"),
                @Server(url = "https://api-staging.monentreprise.com", description = "Serveur de test"),
                @Server(url = "https://api.monentreprise.com", description = "Serveur de production")
        },
        tags = {
                @Tag(name = "Auth", description = "Authentification et profil utilisateur"),
                @Tag(name = "Workouts", description = "Gestion des séances d'entraînement"),
                @Tag(name = "Training Plans", description = "Gestion des plans d'entraînement")
        }
)
@ApplicationScoped
public class OpenApiConfig {
}