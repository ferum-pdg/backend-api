package org.heigvd.resource;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
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
 * Ressource REST pour la gestion des plans d'entraînement.
 *
 * Permet de récupérer le plan en cours et d'en générer un nouveau.
 */
@Path("/training-plan")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Training Plans", description = "Gestion des plans d'entraînement")
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
    EntityManager em;

    @Inject
    WorkoutService workoutService;

    /**
     * Récupère le plan d'entraînement de l'utilisateur authentifié.
     *
     * @param securityContext Contexte de sécurité contenant l'identité JWT
     */
    @GET
    @Operation(
            summary = "Mon plan d'entraînement",
            description = "Retourne le plan d'entraînement de l'utilisateur authentifié s'il existe."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Plan trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrainingPlanLightDto.class))
            ),
            @APIResponse(responseCode = "401", description = "Non authentifié"),
            @APIResponse(responseCode = "404", description = "Plan introuvable"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public Response getMyTrainingPlan(
            @Parameter(description = "Contexte de sécurité avec l'identité JWT", hidden = true)
            SecurityContext securityContext) {
        try {
            UUID accountId = UUID.fromString(securityContext.getUserPrincipal().getName());

            Optional<TrainingPlan> tp = trainingPlanService.getMyTrainingPlan(accountId);

            if (tp.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Plan d'entraînement introuvable\"}")
                        .build();
            }

            TrainingPlanLightDto trainingPlanLightDto = new TrainingPlanLightDto(
                    tp.get().getId(),
                    trainingPlanService.getCurrentWeekNb(tp.get()),
                    tp.get().getWeeklyPlans().size(),
                    tp.get().getWorkouts().size(),
                    tp.get().getWeeklyPlans().stream().mapToInt(wp -> wp.getDailyPlans().size()).sum(),
                    tp.get().getWeeklyPlans().get(trainingPlanService.getCurrentWeekNb(tp.get())-1)
            );

            return Response.ok(trainingPlanLightDto).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }

    /**
     * Génère et crée un plan d'entraînement pour l'utilisateur authentifié.
     *
     * @param securityContext Contexte de sécurité contenant l'identité JWT
     * @param trainingPlanRequestDto Paramètres de génération du plan
     */
    @POST
    @Transactional
    @Operation(
            summary = "Créer un plan d'entraînement",
            description = "Génère et crée un plan d'entraînement personnalisé pour l'utilisateur authentifié."
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "Plan créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrainingPlanResponseDto.class))
            ),
            @APIResponse(responseCode = "400", description = "Paramètres invalides"),
            @APIResponse(responseCode = "401", description = "Non authentifié"),
            @APIResponse(responseCode = "404", description = "Compte utilisateur introuvable"),
            @APIResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @RequestBody(
            description = "Paramètres de génération du plan d'entraînement",
            required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TrainingPlanRequestDto.class))
    )
    public Response createTrainingPlan(
            @Parameter(description = "Contexte de sécurité avec l'identité JWT", hidden = true)
            SecurityContext securityContext,
            @Parameter(description = "Paramètres pour la génération du plan d'entraînement", required = true)
            @Valid TrainingPlanRequestDto trainingPlanRequestDto) {
        try {
            UUID accountId = UUID.fromString(securityContext.getUserPrincipal().getName());

            Optional<Account> account = accountService.findById(accountId);
            if (account.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Compte utilisateur introuvable\"}")
                        .build();
            }

            TrainingPlan newTrainingPlan = tgs.generate(trainingPlanRequestDto, account.get());

            if (newTrainingPlan == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\": \"Échec de la génération du plan d'entraînement\"}")
                        .build();
            }

            trainingPlanService.create(newTrainingPlan);
            workoutService.generateWorkout(newTrainingPlan, LocalDate.now());

            return Response.status(Response.Status.CREATED)
                    .entity(new TrainingPlanResponseDto(newTrainingPlan))
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Erreur interne du serveur\"}")
                    .build();
        }
    }
}