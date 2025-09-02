package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.heigvd.dto.workout_dto.WorkoutFullDto;
import org.heigvd.dto.workout_dto.WorkoutLightDto;
import org.heigvd.dto.workout_dto.WorkoutUploadDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.workout.Workout;
import org.heigvd.service.AccountService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.service.WorkoutAnalyserService;
import org.heigvd.service.WorkoutService;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST resource for workout management.
 *
 * Allows viewing, creating and deleting workouts for the authenticated user.
 */
@Path("/workouts")
@Authenticated
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Tag(name = "Workouts", description = "Workout management")
public class WorkoutResource {

    @Inject
    WorkoutService workoutService;

    @Inject
    WorkoutAnalyserService was;

    @Inject
    AccountService accountService;

    @Inject
    TrainingPlanService trainingPlanService;

    /**
     * Retrieves the upcoming workouts of the authenticated user.
     *
     * @param context Security context containing the JWT identity
     */
    @GET
    @Operation(
            summary = "Get my upcoming workouts",
            description = "Returns the upcoming workouts for the current and next week for the authenticated user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "List of upcoming workouts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkoutLightDto.class))
            ),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getMyNextWorkouts(
            @Parameter(description = "Security context with JWT identity", hidden = true)
            @Context SecurityContext context) {
        try {
            UUID accountId = UUID.fromString(context.getUserPrincipal().getName());

            List<WorkoutLightDto> workoutDtos = new ArrayList<>();

            Optional<TrainingPlan> tp = trainingPlanService.getMyTrainingPlan(accountId);

            // If no training plan, return all workouts
            if(tp.isEmpty()) {
                List<Workout> allWorkouts = workoutService.getAllWorkouts(accountId);
                for(Workout w : allWorkouts) {
                    workoutDtos.add(new WorkoutLightDto(w));
                }
                return Response.ok(workoutDtos).build();
            }

            List<OffsetDateTime> nextWorkoutsDates = trainingPlanService.getDatesForNextWorkouts(accountId);

            List<Workout> nextWorkouts = workoutService.getWorkoutsBetweenDates(
                    accountId,
                    nextWorkoutsDates.getFirst(),
                    nextWorkoutsDates.getLast());

            workoutDtos.addAll(nextWorkouts.stream().map(w -> {
                Integer weekNumber = trainingPlanService.getWeekNumberForDate(tp.get(), w.getStartTime().toLocalDate());
                return new WorkoutLightDto(w, weekNumber);
            }).toList());

            return Response.ok(workoutDtos).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    /**
     * Retrieves all workouts of the authenticated user.
     *
     * @param context Security context containing the JWT identity
     */
    @GET
    @Path("/all")
    @Operation(
            summary = "Get all my workouts",
            description = "Returns all workouts of the authenticated user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Complete list of workouts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkoutLightDto.class))
            ),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllMyWorkouts(
            @Parameter(description = "Security context with JWT identity", hidden = true)
            @Context SecurityContext context) {
        try {
            UUID accountId = UUID.fromString(context.getUserPrincipal().getName());

            List<WorkoutLightDto> workoutDtos = new ArrayList<>();

            List<Workout> allWorkouts = workoutService.getAllWorkouts(accountId);
            for(Workout w : allWorkouts) {
                workoutDtos.add(new WorkoutLightDto(w));
            }
            return Response.ok(workoutDtos).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    /**
     * Creates a new workout from recorded data.
     *
     * @param context Security context containing the JWT identity
     * @param workoutDto Workout data to create
     */
    @POST
    @Transactional
    @Operation(
            summary = "Create a workout",
            description = "Creates a new workout from recorded training data."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Workout created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkoutLightDto.class))
            ),
            @APIResponse(responseCode = "400", description = "Invalid data or account not found"),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestBody(
            description = "Workout data to save",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WorkoutUploadDto.class))
    )
    public Response insertNewRecordedWorkout(
            @Parameter(description = "Security context with JWT identity", hidden = true)
            @Context SecurityContext context,
            @Parameter(description = "Workout data to create", required = true)
            @Valid WorkoutUploadDto workoutDto) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());
            Optional<Account> a = accountService.findById(authenticatedAccountId);

            if(a.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Account not found\"}")
                        .build();
            }

            Optional<Workout> w = workoutService.findClosestWorkout(workoutDto, a.get());
            Workout toReturn;

            if (w.isEmpty()) {
                System.out.println("Creating new workout");
                toReturn = workoutService.createWorkoutOutOfTP(a.get(), workoutDto);
            } else {
                System.out.println("Merging with existing workout");
                toReturn = was.analyse(workoutService.mergeWorkoutWithExisting(w.get(), workoutDto));
            }

            return Response.ok(new WorkoutLightDto(toReturn)).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    /**
     * Retrieves a workout by identifier.
     *
     * @param id Workout identifier
     * @param context Security context containing the JWT identity
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get workout details",
            description = "Returns a workout by identifier if it belongs to the authenticated user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Workout found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkoutFullDto.class))
            ),
            @APIResponse(responseCode = "400", description = "Account not found"),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "403", description = "Access denied"),
            @APIResponse(responseCode = "404", description = "Workout not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getWorkout(
            @Parameter(description = "Unique workout identifier", required = true)
            @PathParam("id") UUID id,
            @Parameter(description = "Security context with JWT identity", hidden = true)
            @Context SecurityContext context){
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            Optional<Account> optAccount = accountService.findById(authenticatedAccountId);

            if (optAccount.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Account not found\"}")
                        .build();
            }

            Account account = optAccount.get();

            Optional<Workout> workoutOpt = workoutService.getWorkoutByID(id);

            if (workoutOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout not found\"}")
                        .build();
            }

            Workout workout = workoutOpt.get();

            if (!workout.getAccount().getId().equals(authenticatedAccountId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"You can only access your own workouts\"}")
                        .build();
            }

            WorkoutFullDto dto = workoutService.toWorkoutFullDto(workout, account.getFCMax());

            return Response.ok(dto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    /**
     * Lists workouts of the authenticated user filtered by sport.
     *
     * @param sport Target sport (e.g.: RUNNING)
     * @param context Security context containing the JWT identity
     */
    @GET
    @Path("/my/sport/{sport}")
    @Operation(
            summary = "Get my workouts by sport",
            description = "Returns workouts filtered by sport for the authenticated user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Filtered list of workouts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Workout.class))
            ),
            @APIResponse(responseCode = "400", description = "Invalid sport"),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getMyWorkoutsBySport(
            @Parameter(
                    description = "Sport type (RUNNING, CYCLING, SWIMMING)",
                    required = true,
                    example = "RUNNING"
            )
            @PathParam("sport") String sport,
            @Parameter(description = "Security context with JWT identity", hidden = true)
            @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            try {
                Sport sportEnum = Sport.valueOf(sport.toUpperCase());
                List<Workout> workouts = workoutService.findByAccountIdAndSport(authenticatedAccountId, sportEnum);
                return Response.ok(workouts).build();
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Invalid sport: " + sport + ". Valid sports are: RUNNING, CYCLING, SWIMMING.\"}")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }

    /**
     * Deletes a workout belonging to the authenticated user.
     *
     * @param id Workout identifier
     * @param context Security context containing the JWT identity
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(
            summary = "Delete a workout",
            description = "Deletes a workout belonging to the authenticated user."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Workout deleted successfully"),
            @APIResponse(responseCode = "401", description = "Not authenticated"),
            @APIResponse(responseCode = "403", description = "Access denied"),
            @APIResponse(responseCode = "404", description = "Workout not found"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response deleteWorkout(
            @Parameter(description = "Unique identifier of the workout to delete", required = true)
            @PathParam("id") UUID id,
            @Parameter(description = "Security context with JWT identity", hidden = true)
            @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            // Check that the workout exists and belongs to the user
            Optional<Workout> workoutOpt = workoutService.getWorkoutByID(id);
            if (workoutOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout not found\"}")
                        .build();
            }

            Workout workout = workoutOpt.get();
            if (!workout.getAccount().getId().equals(authenticatedAccountId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"You can only delete your own workouts\"}")
                        .build();
            }

            boolean deleted = workoutService.delete(id);
            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout not found\"}")
                        .build();
            }

            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error\"}")
                    .build();
        }
    }
}