package org.heigvd.resource;


import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.heigvd.dto.training_plan_dto.TrainingPlanLightDto;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.dto.training_plan_dto.TrainingPlanResponseDto;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.service.AccountService;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_generator.TrainingGeneratorV1;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

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
@SecurityRequirement(name = "bearerAuth")
public class TrainingPlanResource {

    @Inject
    TrainingPlanService trainingPlanService;

    @Inject
    GoalService goalService;

    @Inject
    AccountService accountService;

    @Inject
    TrainingGeneratorV1 trainingGeneratorV1;

    @Inject
    EntityManager em;

    @GET
    /**
     * Récupère le plan d'entraînement de l'utilisateur authentifié.
     *
     * @param securityContext Contexte de sécurité
     */
    @Operation(summary = "Mon plan d'entraînement",
            description = "Retourne le plan d'entraînement de l'utilisateur authentifié s'il existe.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Plan trouvé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrainingPlanResponseDto.class))),
            @APIResponse(responseCode = "404", description = "Plan introuvable")
    })
    public Response getMyTrainingPlan(SecurityContext securityContext) {

        UUID accountId = UUID.fromString(securityContext.getUserPrincipal().getName());

        Optional<TrainingPlan> tp = trainingPlanService.getMyTrainingPlan(accountId);

        if (tp.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Training plan not found").build();
        }

        TrainingPlanLightDto trainingPlanLightDto = new TrainingPlanLightDto(
                tp.get().getId(),
                trainingPlanService.getCurrentWeekNb(tp.get()),
                tp.get().getWeeklyPlans().size(),
                tp.get().getWorkouts().size(),
                tp.get().getWeeklyPlans().stream().mapToInt(wp -> wp.getDailyPlans().size()).sum(),
                tp.get().getWeeklyPlans().get(trainingPlanService.getCurrentWeekNb(tp.get())-1)
        );

        // Assuming the training plan is found, return it
        return Response.ok(trainingPlanLightDto).build();
    }

    @Transactional
    @POST
    /**
     * Génère et crée un plan d'entraînement pour l'utilisateur authentifié.
     *
     * @param securityContext Contexte de sécurité
     * @param trainingPlanRequestDto Paramètres de génération du plan
     */
    @Operation(summary = "Créer un plan d'entraînement",
            description = "Génère et crée un plan d'entraînement pour l'utilisateur authentifié.")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Plan créé",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TrainingPlanResponseDto.class))),
            @APIResponse(responseCode = "400", description = "Paramètres invalides"),
            @APIResponse(responseCode = "404", description = "Compte introuvable")
    })
    @RequestBody(description = "Paramètres de génération du plan", required = true,
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TrainingPlanRequestDto.class)))
    public Response createTrainingPlan(SecurityContext securityContext, TrainingPlanRequestDto trainingPlanRequestDto) {
        UUID accountId = UUID.fromString(securityContext.getUserPrincipal().getName());

        Optional<Account> account = accountService.findById(accountId);
        if (account.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Account not found").build();
        }

        TrainingPlan newTrainingPlan = trainingGeneratorV1.generateTrainingPlan(trainingPlanRequestDto, account.get());

        if (newTrainingPlan == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Failed to generate training plan").build();
        }

        //newTrainingPlan = trainingGeneratorV1.generateTrainingWorkouts(newTrainingPlan);

        trainingPlanService.create(newTrainingPlan);

        return Response.status(Response.Status.CREATED).entity(new TrainingPlanResponseDto(newTrainingPlan)).build();
    }
}
