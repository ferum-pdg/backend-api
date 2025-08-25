package org.heigvd.resource;

import io.quarkus.security.Authenticated;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.persistence.EntityManager;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import org.heigvd.dto.WorkoutDto;
import org.heigvd.dto.WorkoutDto.WorkoutLightDto;
import org.heigvd.dto.WorkoutDto.WorkoutUploadDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.service.AccountService;
import org.heigvd.service.WorkoutService;

import org.jboss.resteasy.reactive.common.util.RestMediaType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Ressource REST pour la gestion des séances d'entraînement (workouts).
 *
 * Permet de consulter, créer et supprimer des workouts pour l'utilisateur authentifié.
 */
@Path("/workouts")
@Authenticated
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Tag(name = "Workouts", description = "Gestion des séances d'entraînement")
@SecurityRequirement(name = "bearerAuth")
public class WorkoutResource {

    @Inject
    WorkoutService workoutService;

    @Inject
    AccountService accountService;


    @GET
    /**
     * Get the nexts n workouts for the authenticated user.
     * @param context SecurityContext to get the authenticated user
     * @return Response containing the list of the nexts n workouts or an error message
     */
    public Response getMyNextWorkouts(@Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

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
    /**
     * Récupère un workout par identifiant.
     *
     * @param id Identifiant du workout
     * @param context Contexte de sécurité
     */
    @Operation(summary = "Détail d'un workout",
            description = "Retourne un workout par identifiant si celui-ci appartient à l'utilisateur authentifié.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Workout trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Workout.class))),
            @APIResponse(responseCode = "403", description = "Accès refusé"),
            @APIResponse(responseCode = "404", description = "Workout introuvable"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getWorkout(
            @Parameter(description = "Identifiant du workout", required = true)
            @PathParam("id") UUID id,
            @Context SecurityContext context){
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
    /**
     * Liste les workouts de l'utilisateur authentifié filtrés par sport.
     *
     * @param sport Sport cible (ex: RUNNING)
     * @param context Contexte de sécurité
     */
    @Operation(summary = "Mes workouts par sport",
            description = "Retourne les workouts filtrés par sport pour l'utilisateur authentifié.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Liste filtrée des workouts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Workout.class))),
            @APIResponse(responseCode = "400", description = "Sport invalide"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getMyWorkoutsBySport(
            @Parameter(description = "Sport (ex: RUNNING, CYCLING)", required = true)
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
    /**
     * Supprime un workout appartenant à l'utilisateur authentifié.
     *
     * @param id Identifiant du workout
     * @param context Contexte de sécurité
     */
    @Operation(summary = "Supprimer un workout",
            description = "Supprime un workout appartenant à l'utilisateur authentifié.")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Supprimé avec succès"),
            @APIResponse(responseCode = "403", description = "Accès refusé"),
            @APIResponse(responseCode = "404", description = "Workout introuvable"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response deleteWorkout(
            @Parameter(description = "Identifiant du workout", required = true)
            @PathParam("id") UUID id,
            @Context SecurityContext context) {
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
    /**
     * (Déprécié) Liste les workouts associés à un plan d'entraînement.
     *
     * @param planId Identifiant du plan
     * @param context Contexte de sécurité
     */
    @Operation(summary = "Mes workouts par plan d'entraînement",
            description = "Retourne les workouts associés à un plan d'entraînement. (Bientôt disponible)",
            deprecated = true)
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Liste des workouts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Workout.class))),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getMyWorkoutsByTrainingPlan(
            @Parameter(description = "Identifiant du plan d'entraînement", required = true)
            @PathParam("planId") UUID planId,
            @Context SecurityContext context) {
        try {
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