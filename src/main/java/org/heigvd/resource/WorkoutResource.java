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
 * Ressource REST pour la gestion des séances d'entraînement (workouts).
 *
 * Permet de consulter, créer et supprimer des workouts pour l'utilisateur authentifié.
 */
@Path("/workouts")
@Authenticated
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Tag(name = "Workouts", description = "Gestion des séances d'entraînement")
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
     * Récupère les prochains workouts de l'utilisateur authentifié.
     *
     * @param context Contexte de sécurité contenant l'identité JWT
     */
    @GET
    @Operation(
            summary = "Mes prochains workouts",
            description = "Retourne les prochains workouts de la semaine courante et suivante pour l'utilisateur authentifié."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste des prochains workouts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkoutLightDto.class))
            ),
            @APIResponse(responseCode = "401", description = "Non authentifié"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getMyNextWorkouts(
            @Parameter(description = "Contexte de sécurité avec l'identité JWT", hidden = true)
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
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }

    /**
     * Récupère tous les workouts de l'utilisateur authentifié.
     *
     * @param context Contexte de sécurité contenant l'identité JWT
     */
    @GET
    @Path("/all")
    @Operation(
            summary = "Tous mes workouts",
            description = "Retourne tous les workouts de l'utilisateur authentifié."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste complète des workouts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkoutLightDto.class))
            ),
            @APIResponse(responseCode = "401", description = "Non authentifié"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getAllMyWorkouts(
            @Parameter(description = "Contexte de sécurité avec l'identité JWT", hidden = true)
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
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }

    /**
     * Crée un nouveau workout à partir des données enregistrées.
     *
     * @param context Contexte de sécurité contenant l'identité JWT
     * @param workoutDto Données du workout à créer
     */
    @POST
    @Transactional
    @Operation(
            summary = "Créer un workout",
            description = "Crée un nouveau workout à partir des données d'entraînement enregistrées."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Workout créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkoutLightDto.class))
            ),
            @APIResponse(responseCode = "400", description = "Données invalides ou compte introuvable"),
            @APIResponse(responseCode = "401", description = "Non authentifié"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @RequestBody(
            description = "Données du workout à enregistrer",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = WorkoutUploadDto.class))
    )
    public Response insertNewRecordedWorkout(
            @Parameter(description = "Contexte de sécurité avec l'identité JWT", hidden = true)
            @Context SecurityContext context,
            @Parameter(description = "Données du workout à créer", required = true)
            @Valid WorkoutUploadDto workoutDto) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());
            Optional<Account> a = accountService.findById(authenticatedAccountId);

            if(a.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Compte introuvable\"}")
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
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }

    /**
     * Récupère un workout par identifiant.
     *
     * @param id Identifiant du workout
     * @param context Contexte de sécurité contenant l'identité JWT
     */
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Détail d'un workout",
            description = "Retourne un workout par identifiant si celui-ci appartient à l'utilisateur authentifié."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Workout trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = WorkoutFullDto.class))
            ),
            @APIResponse(responseCode = "400", description = "Compte introuvable"),
            @APIResponse(responseCode = "401", description = "Non authentifié"),
            @APIResponse(responseCode = "403", description = "Accès refusé"),
            @APIResponse(responseCode = "404", description = "Workout introuvable"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getWorkout(
            @Parameter(description = "Identifiant unique du workout", required = true)
            @PathParam("id") UUID id,
            @Parameter(description = "Contexte de sécurité avec l'identité JWT", hidden = true)
            @Context SecurityContext context){
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            Optional<Account> optAccount = accountService.findById(authenticatedAccountId);

            if (optAccount.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Compte introuvable\"}")
                        .build();
            }

            Account account = optAccount.get();

            Optional<Workout> workoutOpt = workoutService.getWorkoutByID(id);

            if (workoutOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout introuvable\"}")
                        .build();
            }

            Workout workout = workoutOpt.get();

            if (!workout.getAccount().getId().equals(authenticatedAccountId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"Vous ne pouvez accéder qu'à vos propres workouts\"}")
                        .build();
            }

            WorkoutFullDto dto = workoutService.toWorkoutFullDto(workout, account.getFCMax());

            return Response.ok(dto).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }

    /**
     * Liste les workouts de l'utilisateur authentifié filtrés par sport.
     *
     * @param sport Sport cible (ex: RUNNING)
     * @param context Contexte de sécurité contenant l'identité JWT
     */
    @GET
    @Path("/my/sport/{sport}")
    @Operation(
            summary = "Mes workouts par sport",
            description = "Retourne les workouts filtrés par sport pour l'utilisateur authentifié."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Liste filtrée des workouts",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Workout.class))
            ),
            @APIResponse(responseCode = "400", description = "Sport invalide"),
            @APIResponse(responseCode = "401", description = "Non authentifié"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getMyWorkoutsBySport(
            @Parameter(
                    description = "Type de sport (RUNNING, CYCLING, SWIMMING)",
                    required = true,
                    example = "RUNNING"
            )
            @PathParam("sport") String sport,
            @Parameter(description = "Contexte de sécurité avec l'identité JWT", hidden = true)
            @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            try {
                Sport sportEnum = Sport.valueOf(sport.toUpperCase());
                List<Workout> workouts = workoutService.findByAccountIdAndSport(authenticatedAccountId, sportEnum);
                return Response.ok(workouts).build();
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Sport invalide: " + sport + ". Les sports valides sont: RUNNING, CYCLING, SWIMMING.\"}")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }

    /**
     * Supprime un workout appartenant à l'utilisateur authentifié.
     *
     * @param id Identifiant du workout
     * @param context Contexte de sécurité contenant l'identité JWT
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(
            summary = "Supprimer un workout",
            description = "Supprime un workout appartenant à l'utilisateur authentifié."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Workout supprimé avec succès"),
            @APIResponse(responseCode = "401", description = "Non authentifié"),
            @APIResponse(responseCode = "403", description = "Accès refusé"),
            @APIResponse(responseCode = "404", description = "Workout introuvable"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response deleteWorkout(
            @Parameter(description = "Identifiant unique du workout à supprimer", required = true)
            @PathParam("id") UUID id,
            @Parameter(description = "Contexte de sécurité avec l'identité JWT", hidden = true)
            @Context SecurityContext context) {
        try {
            UUID authenticatedAccountId = UUID.fromString(context.getUserPrincipal().getName());

            // Vérifier que le workout existe et appartient à l'utilisateur
            Optional<Workout> workoutOpt = workoutService.getWorkoutByID(id);
            if (workoutOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout introuvable\"}")
                        .build();
            }

            Workout workout = workoutOpt.get();
            if (!workout.getAccount().getId().equals(authenticatedAccountId)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"error\": \"Vous ne pouvez supprimer que vos propres workouts\"}")
                        .build();
            }

            boolean deleted = workoutService.delete(id);
            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Workout introuvable\"}")
                        .build();
            }

            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }
}