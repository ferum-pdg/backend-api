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
import org.heigvd.dto.WorkoutDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.entity.Workout.WorkoutStatus;
import org.heigvd.service.WorkoutService;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import jakarta.persistence.EntityManager;
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
    EntityManager em;

    @GET
    @Path("/{id}")
    /**
     * Récupère un workout par identifiant.
     *
     * @param id Identifiant du workout
     * @param context Contexte de sécurité
     * @return 200 avec le workout, 403 si non propriétaire, 404 si introuvable
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
    /**
     * Liste tous les workouts de l'utilisateur authentifié.
     *
     * @param context Contexte de sécurité
     * @return 200 avec la liste des workouts
     */
    @Operation(summary = "Liste de mes workouts",
            description = "Retourne tous les workouts de l'utilisateur authentifié.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Liste des workouts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Workout.class))) ,
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
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
    /**
     * Liste les workouts de l'utilisateur authentifié filtrés par sport.
     *
     * @param sport Sport cible (ex: RUNNING)
     * @param context Contexte de sécurité
     * @return 200 avec la liste filtrée, 400 si sport invalide
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

    @POST
    @Transactional
    /**
     * Crée un nouveau workout pour l'utilisateur authentifié.
     *
     * @param workoutDto Données du workout à créer
     * @param context Contexte de sécurité
     * @return 201 avec le workout créé, 400 en cas d'entrée invalide
     */
    @Operation(summary = "Créer un workout",
            description = "Crée un nouveau workout pour l'utilisateur authentifié.")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Workout créé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Workout.class))),
            @APIResponse(responseCode = "400", description = "Entrée invalide"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @RequestBody(description = "Données du workout à créer", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WorkoutDto.class)))
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
    /**
     * Supprime un workout appartenant à l'utilisateur authentifié.
     *
     * @param id Identifiant du workout
     * @param context Contexte de sécurité
     * @return 204 si supprimé, 403 si accès refusé, 404 si introuvable
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
    /**
     * (Déprécié) Liste les workouts associés à un plan d'entraînement.
     *
     * @param planId Identifiant du plan
     * @param context Contexte de sécurité
     * @return 200 avec la liste des workouts
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