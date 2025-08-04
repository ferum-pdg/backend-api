package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.entity.*;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_engine.TrainingGeneratorV1;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
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

        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        Goal g2 = goalService.getSpecificGoal(Sport.CYCLING, 20.0);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY);
        //days = List.of(DayOfWeek.SATURDAY);

        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("guillaumetrueb@etik.com", "Guillaume", "Trüeb",
                LocalDate.of(1999, 9, 21), 77.0, 176.0, 205);

        FitnessLevel fitnessLevel = new FitnessLevel(LocalDate.now(), 75);

        account.addFitnessLevel(fitnessLevel);

        TrainingPlan tp = new TrainingPlan(List.of(g1,g2), endDate, days, days, account);

        List<Workout> workouts = trainingGeneratorV1.generateTrainingWorkouts(tp);

        System.out.println(workouts);

    }
}
