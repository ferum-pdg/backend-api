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
 * REST resource for goals management.
 *
 * Provides operations to retrieve available goals,
 * either all goals or filtered by sport.
 */
@Path("/goals")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Tag(name = "Goals", description = "Goals management")
public class GoalResource {

    @Inject
    GoalService goalService;

    /**
     * Returns all available goals.
     */
    @GET
    @Operation(
            summary = "List all goals",
            description = "Returns the complete list of all available goals in the application."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Goals list retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllGoals() {
        try {
            return Response.ok(goalService.getAllGoals()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    /**
     * Returns goals filtered by sport.
     *
     * @param sport The sport for which to retrieve goals (RUNNING, CYCLING, SWIMMING)
     */
    @GET
    @Path("/{sport}")
    @Operation(
            summary = "Goals by sport",
            description = "Returns the list of available goals for a specific sport."
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Sport goals retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @APIResponse(responseCode = "400", description = "Invalid sport"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getGoalsBySport(
            @Parameter(
                    description = "Sport type (RUNNING, CYCLING, SWIMMING)",
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
                        .entity("{\"error\": \"Invalid sport. Valid sports are: RUNNING, CYCLING, SWIMMING.\"}")
                        .build();
            }
            return Response.ok(goalService.getGoalsBySport(sportEnum)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }
}