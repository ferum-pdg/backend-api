package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.heigvd.dto.WorkoutDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.entity.Workout.WorkoutStatus;
import org.heigvd.service.WorkoutService;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import jakarta.persistence.EntityManager;
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

    @Inject
    EntityManager em;

    @GET
    @Path("/{id}")
    public Response getWorkout(@PathParam("id") UUID id, @Context SecurityContext context){
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            Optional<Workout> workoutOpt = workoutService.findById(id);

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

            List<Workout> workouts = workoutService.findByAccountId(authenticatedAccountId);

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
                Sport sportEnum = Sport.valueOf(sport.toUpperCase());
                List<Workout> workouts = workoutService.findByAccountIdAndSport(authenticatedAccountId, sportEnum);
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

    @POST
    @Transactional
    public Response createWorkout(@Valid WorkoutDto workoutDto, @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            // S'assurer que le workout est créé pour le compte authentifié
            workoutDto.setAccountId(authenticatedAccountId);

            // Récupérer le compte
            Account account = em.find(Account.class, authenticatedAccountId);
            if (account == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Account not found\"}")
                        .build();
            }

            // Créer le workout
            Workout workout = new Workout(
                    account,
                    Sport.valueOf(workoutDto.getSport().toUpperCase()),
                    workoutDto.getStartTime(),
                    workoutDto.getEndTime(),
                    workoutDto.getSource(),
                    WorkoutStatus.valueOf(workoutDto.getStatus().toUpperCase()),
                    null, // plannedDataPoints
                    null  // workoutType
            );

            // Définir les autres propriétés
            if (workoutDto.getDurationSec() != null) {
                workout.setDurationSec((int) workoutDto.getDurationSec().longValue());
            }
            if (workoutDto.getDistanceMeters() != null) {
                workout.setDistanceMeters(workoutDto.getDistanceMeters());
            }
            if (workoutDto.getCaloriesKcal() != null) {
                workout.setCaloriesKcal(workoutDto.getCaloriesKcal().intValue());
            }
            if (workoutDto.getAvgHeartRate() != null) {
                workout.setAvgHeartRate(workoutDto.getAvgHeartRate());
            }
            if (workoutDto.getMaxHeartRate() != null) {
                workout.setMaxHeartRate(workoutDto.getMaxHeartRate());
            }
            if (workoutDto.getAverageSpeed() != null) {
                workout.setAverageSpeed(workoutDto.getAverageSpeed());
            }

            Workout createdWorkout = workoutService.create(workout);
            return Response.status(Response.Status.CREATED).entity(createdWorkout).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid input: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteWorkout(@PathParam("id") UUID id, @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            // Vérifier que le workout existe et appartient à l'utilisateur
            Optional<Workout> workoutOpt = workoutService.findById(id);
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

            // TODO: Implémenter cette méthode quand elle sera disponible dans le service
            //List<Workout> workouts = workoutService.findByTrainingPlan(authenticatedAccountId, planId);
            List<Workout> workouts = null;

            return Response.ok(workouts).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}