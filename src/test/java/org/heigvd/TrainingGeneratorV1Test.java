package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.entity.*;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.Workout.Workout;
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
    void generateTrainingWithOneGoal() {

        // 2 training per weeks
        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 10.0);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("guillaumetrueb@etik.com", "Guillaume", "Trüeb",
                LocalDate.of(1999, 9, 21), 77.0, 176.0, 205);

        FitnessLevel fitnessLevel = new FitnessLevel(LocalDate.now(), 75);

        account.addFitnessLevel(fitnessLevel);

        TrainingPlan tp = new TrainingPlan(List.of(g1), endDate, days, days, account);

        try {
            trainingGeneratorV1.generateTrainingWorkouts(tp);
        } catch (Exception e) {
            System.out.println("Not enough available days to schedule the workouts.");
            return;
        }

        System.out.println("generateTrainingWithOneGoal: \n\n" + tp);
    }

    @Test
    void generateTrainingWithTwoGoals() {

        // 2 training per weeks
        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 10.0);

        // 2 training per weeks
        Goal g2 = goalService.getSpecificGoal(Sport.CYCLING, 20.0);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("guillaumetrueb@etik.com", "Guillaume", "Trüeb",
                LocalDate.of(1999, 9, 21), 77.0, 176.0, 205);

        FitnessLevel fitnessLevel = new FitnessLevel(LocalDate.now(), 75);

        account.addFitnessLevel(fitnessLevel);

        TrainingPlan tp = new TrainingPlan(List.of(g1,g2), endDate, days, days, account);

        try {
            trainingGeneratorV1.generateTrainingWorkouts(tp);
        } catch (Exception e) {
            System.out.println("Not enough available days to schedule the workouts.");
            return;
        }

        System.out.println(tp);
    }

    @Test
    void generateTrainingWithThreeGoals() {

        // 2 training per weeks
        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 10.0);

        // 2 training per weeks
        Goal g2 = goalService.getSpecificGoal(Sport.CYCLING, 20.0);

        // 2 training per weeks
        Goal g3 = goalService.getSpecificGoal(Sport.SWIMMING, 1.0);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("guillaumetrueb@etik.com", "Guillaume", "Trüeb",
                LocalDate.of(1999, 9, 21), 77.0, 176.0, 205);

        FitnessLevel fitnessLevel = new FitnessLevel(LocalDate.now(), 75);

        account.addFitnessLevel(fitnessLevel);

        TrainingPlan tp = new TrainingPlan(List.of(g1,g2,g3), endDate, days, days, account);

        try {
            trainingGeneratorV1.generateTrainingWorkouts(tp);
        } catch (Exception e) {
            System.out.println("Not enough available days to schedule the workouts.");
            return;
        }

        System.out.println(tp);
    }
}
