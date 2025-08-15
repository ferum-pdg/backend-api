package org.heigvd.resource;


import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.heigvd.dto.TrainingPlanRequestDto;
import org.heigvd.dto.TrainingPlanResponseDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.FitnessLevel;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.Workout.TrainingPlanPhase;
import org.heigvd.service.AccountService;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_engine.TrainingGeneratorV1;
import org.jboss.resteasy.reactive.common.util.RestMediaType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
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

        // Assuming the training plan is found, return it
        return Response.ok(new TrainingPlanResponseDto(tp.get())).build();
    }

    @Transactional
    @POST
    public Response createTrainingPlan(SecurityContext securityContext, TrainingPlanRequestDto trainingPlanRequestDto) {
        UUID accountId = UUID.fromString(securityContext.getUserPrincipal().getName());

        Optional<Account> account = accountService.findById(accountId);
        if (account.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Account not found").build();
        }

        List<Goal> goals = goalService.getGoalsByIds(trainingPlanRequestDto.getGoalIds());
        if (goals.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("No valid goals provided").build();
        }

        List<DayOfWeek> daysOfWeek = trainingPlanRequestDto.getDaysOfWeek().stream()
                .map(DayOfWeek::valueOf)
                .toList();

        LocalDate endDate = trainingPlanRequestDto.getEndDate();

        TrainingPlan tp = new TrainingPlan(goals, endDate, daysOfWeek, daysOfWeek, account.get());
        tp.setCurrentPhase(TrainingPlanPhase.BASE);

        TrainingPlan newTrainingPlan = trainingGeneratorV1.generateTrainingWorkouts(tp);

        trainingPlanService.create(newTrainingPlan);

        return Response.status(Response.Status.CREATED).entity(new TrainingPlanResponseDto(newTrainingPlan)).build();
    }
}
