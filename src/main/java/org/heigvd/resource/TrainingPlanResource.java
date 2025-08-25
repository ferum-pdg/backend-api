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
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.service.AccountService;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_generator.TrainingGeneratorV1;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import java.util.Optional;
import java.util.UUID;

@Path("/training-plan")
@Produces(RestMediaType.APPLICATION_JSON)
@Consumes(RestMediaType.APPLICATION_JSON)
@Authenticated
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
