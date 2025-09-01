package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.heigvd.entity.Sport;
import org.heigvd.service.GoalService;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

/**
 * Ressource REST de gestion des objectifs.
 *
 * Fournit les opérations de consultation des objectifs disponibles,
 * soit tous les objectifs, soit filtrés par sport.
 */
@Path("/goals")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Tag(name = "Goals", description = "Gestion des objectifs")
public class GoalResource {

    @Inject
    GoalService goalService;

    /**
     * Retourne tous les objectifs disponibles.
     */
    @GET
    @Operation(
            summary = "Liste tous les objectifs",
            description = "Retourne la liste complète de tous les objectifs disponibles dans l'application."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des objectifs récupérée avec succès",
                    content = @Content(mediaType = "application/json")
            ),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getAllGoals() {
        try {
            return Response.ok(goalService.getAllGoals()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }

    /**
     * Retourne les objectifs filtrés par sport.
     *
     * @param sport Le sport pour lequel récupérer les objectifs (RUNNING, CYCLING, SWIMMING)
     */
    @GET
    @Path("/{sport}")
    @Operation(
            summary = "Objectifs par sport",
            description = "Retourne la liste des objectifs disponibles pour un sport spécifique."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Objectifs du sport récupérés avec succès",
                    content = @Content(mediaType = "application/json")
            ),
            @APIResponse(responseCode = "400", description = "Sport invalide"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getGoalsBySport(
            @Parameter(
                    description = "Type de sport (RUNNING, CYCLING, SWIMMING)",
                    required = true
            )
            @PathParam("sport") String sport) {
        try {
            // First we need to check if the sport is valid
            Sport sportEnum;
            try {
                sportEnum = Sport.valueOf(sport.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Sport invalide. Les sports valides sont: RUNNING, CYCLING, SWIMMING.\"}")
                        .build();
            }
            return Response.ok(goalService.getGoalsBySport(sportEnum)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }
}