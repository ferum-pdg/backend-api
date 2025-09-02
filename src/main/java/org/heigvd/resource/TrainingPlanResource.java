package org.heigvd.resource;


import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.heigvd.dto.training_plan_dto.TrainingPlanLightDto;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.dto.training_plan_dto.TrainingPlanResponseDto;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.heigvd.dto.training_plan_dto.TrainingPlanLightDto;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.dto.training_plan_dto.TrainingPlanResponseDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.service.*;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * REST resource for training plan management.
 *
 * Allows retrieving the current plan and generating a new one.
 */
@Path("/training-plan")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Training Plans", description = "Training plan management")
@SecurityRequirement(name = "bearerAuth")
public class TrainingPlanResource {

    @Inject
    TrainingPlanService trainingPlanService;

    @Inject
    GoalService goalService;

    @Inject
    AccountService accountService;

    @Inject
    TrainingGeneratorService tgs;

    @Inject
    WorkoutService workoutService;

    @Inject
    EntityManager em;

    /**
     * Retrieves the training plan of the authenticated user.
     *
     * @param securityContext Security context containing the JWT identity
     */
    @GET
    @Operation(
            summary = "Get my training plan",
            description = "Returns the training plan of the authenticated user if it exists."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Training plan found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrainingPlanLightDto.class))
            ),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "404", description = "Training plan not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getMyTrainingPlan(
            @Parameter(description = "Security context with JWT identity", hidden = true)
            SecurityContext securityContext) {
        try {
            UUID accountId = UUID.fromString(securityContext.getUserPrincipal().getName());

            Optional<TrainingPlan> tp = trainingPlanService.getMyCurrentTrainingPlan(accountId);

            if (tp.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Training plan not found\"}")
                        .build();
            }

            TrainingPlanLightDto trainingPlanLightDto = new TrainingPlanLightDto(
                    tp.get().getId(),
                    trainingPlanService.getCurrentWeekNb(tp.get()),
                    tp.get().getWeeklyPlans().size(),
                    workoutService.getAllGeneratedWorkouts(accountId, tp.get().getId()).size(),
                    tp.get().getWeeklyPlans().stream().mapToInt(wp -> wp.getDailyPlans().size()).sum(),
                    tp.get().getWeeklyPlans().get(trainingPlanService.getCurrentWeekNb(tp.get())-1)
            );

            return Response.ok(trainingPlanLightDto).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    @POST
    @Transactional
    @Operation(
            summary = "Create a training plan",
            description = "Generates and creates a personalized training plan for the authenticated user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "Training plan created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrainingPlanResponseDto.class))
            ),
            @APIResponse(responseCode = "400", description = "Invalid parameters"),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "404", description = "User account not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestBody(
            description = "Training plan generation parameters",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TrainingPlanRequestDto.class))
    )
    public Response createTrainingPlan(
            @Parameter(description = "Security context with JWT identity", hidden = true)
            SecurityContext securityContext,
            @Parameter(description = "Parameters for training plan generation", required = true)
            @Valid TrainingPlanRequestDto trainingPlanRequestDto) {
        try {
            UUID accountId = UUID.fromString(securityContext.getUserPrincipal().getName());

            Optional<Account> account = accountService.findById(accountId);
            if (account.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"User account not found\"}")
                        .build();
            }

            TrainingPlan newTrainingPlan = tgs.generate(trainingPlanRequestDto, account.get());

            if (newTrainingPlan == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Training plan generation failed\"}")
                        .build();
            }

            trainingPlanService.create(newTrainingPlan);
            workoutService.generateWorkout(newTrainingPlan, LocalDate.now());

            return Response.status(Response.Status.CREATED)
                    .entity(new TrainingPlanResponseDto(newTrainingPlan))
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }
}
