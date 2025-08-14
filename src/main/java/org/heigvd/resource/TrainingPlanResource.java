package org.heigvd.resource;


import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
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
    public Response getMyTrainingPlan() {
        UUID accountId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

        createNewTrainingPlan();

        Optional<TrainingPlan> tp = trainingPlanService.getMyTrainingPlan(accountId);

        if (tp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Training plan not found").build();
        }

        // Assuming the training plan is found, return it
        return Response.ok(tp).build();
    }

    @Transactional
    public void createNewTrainingPlan() {
        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Optional<Account> account = accountService.findById(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"));

        // Persist the fitness level

        FitnessLevel fitnessLevel = new FitnessLevel(LocalDate.now(), 75);
        account.get().addFitnessLevel(fitnessLevel);
        em.merge(account.get());

        TrainingPlan tp = new TrainingPlan(List.of(g1), endDate, days, days, account.get());

        tp.setCurrentPhase(TrainingPlanPhase.BASE);

        TrainingPlan newTrainingPlan = trainingGeneratorV1.generateTrainingWorkouts(tp);

        trainingPlanService.create(newTrainingPlan);

    }
}
