package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.entity.*;
import org.heigvd.entity.TrainingPlan.DailyPlan;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.Workout.TrainingPlanPhase;
import org.heigvd.service.GoalService;
import org.heigvd.training_engine.TrainingGeneratorV1;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class TrainingGeneratorV1Test {

    @Inject
    GoalService goalService;

    @Inject
    TrainingGeneratorV1 trainingGeneratorV1;

    @Test
    void generateTrainingWithOneGoal() {
        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("guillaumetrueb@etik.com", "Guillaume", "Trüeb",
                LocalDate.of(1999, 9, 21), 77.0, 176.0, 205);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 75));

        TrainingPlan tp = new TrainingPlan(List.of(g1), endDate, days, days, account);

        tp.setCurrentPhase(TrainingPlanPhase.BASE);

        trainingGeneratorV1.generateTrainingWorkouts(tp);

        System.out.println("generateTrainingWithOneGoal: \n\n" + tp);
    }

    @Test
    void generateTrainingWithTwoGoals() {
        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        Goal g2 = goalService.getSpecificGoal(Sport.CYCLING, 20.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("guillaumetrueb@etik.com", "Guillaume", "Trüeb",
                LocalDate.of(1999, 9, 21), 77.0, 176.0, 205);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 80));

        TrainingPlan tp = new TrainingPlan(List.of(g1, g2), endDate, days, days, account);

        tp.setCurrentPhase(TrainingPlanPhase.BASE);

        trainingGeneratorV1.generateTrainingWorkouts(tp);

        System.out.println(tp);
    }

    @Test
    void generateTrainingWithThreeGoals() {
        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        Goal g2 = goalService.getSpecificGoal(Sport.CYCLING, 20.0);
        Goal g3 = goalService.getSpecificGoal(Sport.SWIMMING, 1.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("guillaumetrueb@etik.com", "Guillaume", "Trüeb",
                LocalDate.of(1999, 9, 21), 77.0, 176.0, 205);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 67));

        TrainingPlan tp = new TrainingPlan(List.of(g1, g2, g3), endDate, days, days, account);

        tp.setCurrentPhase(TrainingPlanPhase.BASE);

        trainingGeneratorV1.generateTrainingWorkouts(tp);

        System.out.println(tp);
    }

    @Test
    void testSimpleRunningSpacing() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);
        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("runner@etik.com", "Test", "Runner",
                LocalDate.of(1990, 1, 1), 70.0, 175.0, 180);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 70));

        TrainingPlan tp = new TrainingPlan(List.of(running), endDate, days, days, account);

        tp.setCurrentPhase(TrainingPlanPhase.BASE);

        trainingGeneratorV1.generateTrainingWorkouts(tp);

        long mondayCount = tp.getPairWeeklyPlans().stream().filter(dp -> dp.getDayOfWeek() == DayOfWeek.MONDAY).count();
        long wednesdayCount = tp.getPairWeeklyPlans().stream().filter(dp -> dp.getDayOfWeek() == DayOfWeek.FRIDAY).count();

        System.out.println(tp);

        assertEquals(1, mondayCount, "Should have one session on Monday.");
        assertEquals(1, wednesdayCount, "Should have one session on Wednesday.");
    }

    @Test
    void testSimpleRunningSpacingLowLevel() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("runner@etik.com", "Test", "Runner",
                LocalDate.of(1990, 1, 1), 70.0, 175.0, 180);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 55));

        TrainingPlan tp = new TrainingPlan(List.of(running), endDate, days, days, account);

        tp.setCurrentPhase(TrainingPlanPhase.BASE);

        trainingGeneratorV1.generateTrainingWorkouts(tp);

        long mondayCount = tp.getPairWeeklyPlans().stream().filter(dp -> dp.getDayOfWeek() == DayOfWeek.MONDAY).count();

        System.out.println(tp);

        assertEquals(1, mondayCount, "Should have one session on Monday.");
    }

    @Test
    void testMultiSportBalancedDistribution() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 20.0);
        Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 1.0);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

        Account account = new Account("triathlete@etik.com", "Test", "Athlete",
                LocalDate.of(1995, 6, 15), 72.0, 178.0, 185);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 90));

        TrainingPlan tp = new TrainingPlan(List.of(running, cycling, swimming), LocalDate.of(2025, 12, 24), days, days, account);

        tp.setCurrentPhase(TrainingPlanPhase.BASE);

        trainingGeneratorV1.generateTrainingWorkouts(tp);

        System.out.println(tp);

        assertEquals(6, tp.getPairWeeklyPlans().size(), "Expected 6 workouts (2 per sport)");

        for (int i = 1; i < tp.getPairWeeklyPlans().size(); i++) {
            DailyPlan prev = tp.getPairWeeklyPlans().get(i - 1);
            DailyPlan curr = tp.getPairWeeklyPlans().get(i);
            assertNotEquals(prev.getSport(), curr.getSport(), "Same sport on consecutive days");
        }
    }

    @Test
    void testTooManyWorkoutsNotEnoughDays() {
        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 42.2); // 4 workouts/week
        Goal g2 = goalService.getSpecificGoal(Sport.CYCLING, 100.0); // 4 workouts/week

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);

        Account account = new Account("dense@etik.com", "Too", "Dense",
                LocalDate.of(1993, 2, 12), 74.0, 177.0, 180);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 50));

        TrainingPlan tp = new TrainingPlan(List.of(g1, g2), LocalDate.of(2025, 12, 24), days, days, account);

        tp.setCurrentPhase(TrainingPlanPhase.BASE);

        assertThrows(Exception.class, () -> trainingGeneratorV1.generateTrainingWorkouts(tp),
                "Expected exception due to too many workouts for limited days.");
    }
}
