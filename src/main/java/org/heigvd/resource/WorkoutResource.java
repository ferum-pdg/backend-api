package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.heigvd.dto.WorkoutDto;
import org.heigvd.service.WorkoutService;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/workouts")
@Authenticated
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
public class WorkoutResource {

    @Inject
    WorkoutService workoutService;

    @GET
    @Path("/{id}")
    public Response getWorkout(@PathParam("id") UUID id, @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            Optional<WorkoutDto> workoutOpt = workoutService.findById(id);

            if (workoutOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout not found\"}")
                        .build();
            }

            WorkoutDto workout = workoutOpt.get();

            // Vérifier que l'utilisateur authentifié est propriétaire du workout
            if (!workout.getAccountId().equals(authenticatedAccountId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"You can only access your own workouts\"}")
                        .build();
            }

            return Response.ok(workout).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/my")
    public Response getMyWorkouts(@Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            List<WorkoutDto> workouts = workoutService.findByAccountId(authenticatedAccountId);

            return Response.ok(workouts).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/my/sport/{sport}")
    public Response getMyWorkoutsBySport(
            @PathParam("sport") String sport,
            @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            try {
                org.heigvd.entity.Sport sportEnum = org.heigvd.entity.Sport.valueOf(sport.toUpperCase());
                List<WorkoutDto> workouts = workoutService.findByAccountIdAndSport(authenticatedAccountId, sportEnum);
                return Response.ok(workouts).build();
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Invalid sport: " + sport + "\"}")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/my/date-range")
    public Response getMyWorkoutsByDateRange(
            @QueryParam("startDate") String startDateStr,
            @QueryParam("endDate") String endDateStr,
            @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            if (startDateStr == null || endDateStr == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"startDate and endDate parameters are required\"}")
                        .build();
            }

            OffsetDateTime startDate = OffsetDateTime.parse(startDateStr);
            OffsetDateTime endDate = OffsetDateTime.parse(endDateStr);

            List<WorkoutDto> workouts = workoutService.findByDateRange(authenticatedAccountId, startDate, endDate);

            return Response.ok(workouts).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Transactional
    public Response createWorkout(@Valid WorkoutDto workoutDto, @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            // S'assurer que le workout est créé pour le compte authentifié
            workoutDto.setAccountId(authenticatedAccountId);

            WorkoutDto createdWorkout = workoutService.create(workoutDto);
            return Response.status(Response.Status.CREATED).entity(createdWorkout).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateWorkout(
            @PathParam("id") UUID id,
            @Valid WorkoutDto workoutDto,
            @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            // Vérifier que le workout existe et appartient à l'utilisateur
            Optional<WorkoutDto> existingWorkoutOpt = workoutService.findById(id);
            if (existingWorkoutOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout not found\"}")
                        .build();
            }

            WorkoutDto existingWorkout = existingWorkoutOpt.get();
            if (!existingWorkout.getAccountId().equals(authenticatedAccountId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"You can only update your own workouts\"}")
                        .build();
            }

            // S'assurer que l'accountId reste le même
            workoutDto.setAccountId(authenticatedAccountId);

            Optional<WorkoutDto> updatedWorkoutOpt = workoutService.update(id, workoutDto);
            if (updatedWorkoutOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout not found\"}")
                        .build();
            }

            return Response.ok(updatedWorkoutOpt.get()).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response patchWorkout(
            @PathParam("id") UUID id,
            WorkoutDto workoutDto,
            @Context SecurityContext context) {
        // Même logique que PUT pour la mise à jour partielle
        return updateWorkout(id, workoutDto, context);
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteWorkout(@PathParam("id") UUID id, @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            // Vérifier que le workout existe et appartient à l'utilisateur
            Optional<WorkoutDto> workoutOpt = workoutService.findById(id);
            if (workoutOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout not found\"}")
                        .build();
            }

            WorkoutDto workout = workoutOpt.get();
            if (!workout.getAccountId().equals(authenticatedAccountId)) {
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
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/my/training-plan/{planId}")
    public Response getMyWorkoutsByTrainingPlan(
            @PathParam("planId") UUID planId,
            @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            List<WorkoutDto> workouts = workoutService.findByTrainingPlan(authenticatedAccountId, planId);

            return Response.ok(workouts).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}