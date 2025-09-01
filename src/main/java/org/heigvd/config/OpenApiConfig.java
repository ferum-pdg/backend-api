package org.heigvd.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "API ferum",
                version = "1.0.0",
                description = "Cette API permet la gestion des seances d'entrainement (workouts), des plans d'entrainement, ainsi que l'authentification et le profil utilisateur."
        )
)
@SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer Token"
)
public class OpenApiConfig {
}