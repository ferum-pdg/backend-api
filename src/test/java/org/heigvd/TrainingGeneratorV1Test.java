package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.dto.TrainingPlanRequestDto;
import org.heigvd.entity.*;
import org.heigvd.entity.TrainingPlan.DailyPlan;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.service.GoalService;
import org.heigvd.training_generator.TrainingGeneratorV1;
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

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(endDate, days, List.of(g1), true);
        TrainingPlan tp = trainingGeneratorV1.generateTrainingPlan(dto, account);

        trainingGeneratorV1.generateTrainingWorkouts(tp);
        System.out.println(tp);
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

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(endDate, days, List.of(g1, g2), true);
        TrainingPlan tp = trainingGeneratorV1.generateTrainingPlan(dto, account);

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

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(endDate, days, List.of(g1, g2, g3), true);
        TrainingPlan tp = trainingGeneratorV1.generateTrainingPlan(dto, account);

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

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(endDate, days, List.of(running), true);
        TrainingPlan tp = trainingGeneratorV1.generateTrainingPlan(dto, account);

        trainingGeneratorV1.generateTrainingWorkouts(tp);

        long mondayCount = tp.getWeeklyPlans().getFirst().getDailyPlans()
                .stream().filter(dp -> dp.getDayOfWeek() == DayOfWeek.MONDAY).count();

        long fridayCount = tp.getWeeklyPlans().getFirst().getDailyPlans()
                .stream().filter(dp -> dp.getDayOfWeek() == DayOfWeek.FRIDAY).count();

        assertEquals(1, mondayCount);
        assertEquals(1, fridayCount);
    }

    @Test
    void testSimpleRunningSpacingLowLevel() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("runner@etik.com", "Test", "Runner",
                LocalDate.of(1990, 1, 1), 70.0, 175.0, 180);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 55));

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(endDate, days, List.of(running), true);
        TrainingPlan tp = trainingGeneratorV1.generateTrainingPlan(dto, account);

        trainingGeneratorV1.generateTrainingWorkouts(tp);

        long mondayCount = tp.getWeeklyPlans().getFirst().getDailyPlans()
                .stream().filter(dp -> dp.getDayOfWeek() == DayOfWeek.MONDAY).count();

        assertEquals(1, mondayCount);
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

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(LocalDate.of(2025, 12, 24), days, List.of(running, cycling, swimming), true);
        TrainingPlan tp = trainingGeneratorV1.generateTrainingPlan(dto, account);

        trainingGeneratorV1.generateTrainingWorkouts(tp);

        assertEquals(6, tp.getWeeklyPlans().getFirst().getDailyPlans().size());

        for (int i = 1; i < tp.getWeeklyPlans().getFirst().getDailyPlans().size(); i++) {
            DailyPlan prev = tp.getWeeklyPlans().getFirst().getDailyPlans().get(i - 1);
            DailyPlan curr = tp.getWeeklyPlans().getFirst().getDailyPlans().get(i);
            assertNotEquals(prev.getSport(), curr.getSport());
        }
    }

    @Test
    void testTooManyWorkoutsNotEnoughDays() {
        Goal g1 = goalService.getSpecificGoal(Sport.RUNNING, 42.2);
        Goal g2 = goalService.getSpecificGoal(Sport.CYCLING, 100.0);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);

        Account account = new Account("dense@etik.com", "Too", "Dense",
                LocalDate.of(1993, 2, 12), 74.0, 177.0, 180);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 50));

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(LocalDate.of(2025, 12, 24), days, List.of(g1, g2), true);

        assertThrows(Exception.class, () -> trainingGeneratorV1.generateTrainingPlan(dto, account));
    }

}
