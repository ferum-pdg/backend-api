package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.heigvd.dto.WorkoutDto.WorkoutLightDto;
import org.heigvd.dto.WorkoutDto.WorkoutUploadDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.service.AccountService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.service.WorkoutService;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

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
    AccountService accountService;

    @Inject
    TrainingPlanService trainingPlanService;

    /**
     * Get the nexts n workouts for the authenticated user.
     * @param context SecurityContext to get the authenticated user
     * @return Response containing the list of the nexts n workouts or an error message
     */
    @GET
    public Response getMyNextWorkouts(@Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            Integer currentWeek = trainingPlanService.getCurrentWeekNbForUser(authenticatedAccountId);

            List<Workout> currentWeekWorkouts = workoutService.getCurrentWeekWorkouts(authenticatedAccountId);

            List<Workout> workouts = workoutService.getNextNWorkouts(authenticatedAccountId);

            List<WorkoutLightDto> workoutDtos = workouts.stream()
                    .map(WorkoutLightDto::new)
                    .toList();

            return Response.ok(workoutDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Transactional
    public Response insertNewRecordedWorkout(@Context SecurityContext context, @Valid WorkoutUploadDto workout) {
        UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());
        Optional<Account> a = accountService.findById(authenticatedAccountId);

        if(a.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Account not found\"}")
                    .build();
        }

        Optional<Workout> w = workoutService.findClosestWorkout(workout, a.get());

        Workout toReturn;

        if (w.isEmpty()) {
            System.out.println("Creating new workout");
            toReturn = workoutService.createWorkoutOutOfTP(a.get(), workout);
        } else {
            System.out.println("Merging with existing workout");
            toReturn = workoutService.mergeWorkoutWithExisting(w.get(), workout);
        }

        return Response.ok(new WorkoutLightDto(toReturn)).build();
    }


    @GET
    @Path("/{id}")
    public Response getWorkout(@PathParam("id") UUID id, @Context SecurityContext context){
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

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

            return Response.ok(workout).build();
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


    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteWorkout(@PathParam("id") UUID id, @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            // Vérifier que le workout existe et appartient à l'utilisateur
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