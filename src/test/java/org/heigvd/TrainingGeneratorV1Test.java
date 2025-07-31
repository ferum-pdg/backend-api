package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.entity.*;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_engine.TrainingGeneratorV1;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@QuarkusTest
public class TrainingGeneratorV1Test {

    @Inject
    TrainingPlanService trainingPlanService;

    @Inject
    GoalService goalService;

    @Inject
    TrainingGeneratorV1 trainingGeneratorV1;

    @Test
    void generateTraining() {

        Goal g = goalService.getSpecificGoal(Sport.RUNNING, 5.0);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY);

        LocalDate startDate = LocalDate.of(2025, 5, 24);

        TrainingPlan tp = new TrainingPlan(g, startDate, days, days);

        Account account = new Account("guillaumetrueb@etik.com", "Guillaume", "Trüeb",
                LocalDate.of(1999, 9, 21), 77.0, 176.0, 205);

        List<Workout> workouts = trainingGeneratorV1.generateTrainingWorkouts(account, tp);

        System.out.println("Generated " + workouts.size() + " workouts for the training plan.");

        System.out.println(workouts);

    }
}
