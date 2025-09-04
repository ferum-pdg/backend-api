package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
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
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.service.AccountService;
import org.heigvd.service.TrainingGeneratorService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.service.WorkoutAnalyserService;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * REST resource for data synchronization.
 *
 * Allows synchronizing user data.
 */
@Path("/sync")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Sync", description = "Data synchronization")
public class SyncResource {

    /**
     * Synchronize user data when requested.
     *
     * @param context security context containing the JWT identity
     */
    @Inject
    TrainingGeneratorService tgs;

    @Inject
    TrainingPlanService trainingPlanService;

    @POST
    @Operation(
            summary = "Data synchronization",
            description = "Triggers data synchronization for the authenticated user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Synchronization successful"),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response sync(
            @Parameter(description = "Security context with JWT identity", hidden = true)
            SecurityContext securityContext) {

        LocalDate today = LocalDate.now();

        UUID accountId = UUID.fromString(securityContext.getUserPrincipal().getName());

        Optional<TrainingPlan> tp = trainingPlanService.getMyCurrentTrainingPlan(accountId);

        if (tp.isPresent()) {
            tgs.sync(tp.get(), today);
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("No current training plan found to sync.").build();
        }

        return Response.ok().build();
    }
}