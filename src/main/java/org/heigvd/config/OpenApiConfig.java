package org.heigvd.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@OpenAPIDefinition(
        info = @Info(
                title = "API de Gestion Utilisateurs",
                version = "1.0.0",
                description = """
            Cette API permet la gestion complète des utilisateurs et de leurs données.
            
            ## Fonctionnalités principales :
            - Gestion des utilisateurs (CRUD)
            - Authentification et autorisation
            - Gestion des profils et préférences
            
            ## Authentification
            L'API utilise l'authentification Bearer Token (JWT).
            """,
                contact = @Contact(
                        name = "Équipe API",
                        email = "api-support@monentreprise.com",
                        url = "https://monentreprise.com/support"
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
                @Tag(name = "Users", description = "Opérations sur les utilisateurs"),
                @Tag(name = "Auth", description = "Authentification et autorisation"),
                @Tag(name = "Admin", description = "Opérations administrateur")
        }
)
@ApplicationScoped
public class OpenApiConfig {
}