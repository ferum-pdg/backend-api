package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

/**
 * Ressource REST de synchronisation des données.
 *
 * Fournit les opérations de synchronisation pour l'utilisateur authentifié.
 */
@Path("/sync")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Sync", description = "Synchronisation des données")
public class SyncResource {

    /**
     * Synchronise les données de l'utilisateur authentifié.
     *
     * @param context Contexte de sécurité contenant l'identité JWT
     */
    @POST
    @Operation(
            summary = "Synchronisation des données",
            description = "Lance la synchronisation des données pour l'utilisateur authentifié."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Synchronisation réussie"),
            @APIResponse(responseCode = "401", description = "Non authentifié"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response sync(
            @Parameter(description = "Contexte de sécurité avec l'identité JWT", hidden = true)
            SecurityContext context) {
        try {
            // Logique de synchronisation à implémenter
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }
}