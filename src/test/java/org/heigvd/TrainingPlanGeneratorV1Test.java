package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.FitnessLevel;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.DailyPlan;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_generator.TrainingGeneratorV1;
import org.heigvd.training_generator.generator_V1.TrainingPlanGeneratorV1;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
public class TrainingPlanGeneratorV1Test {

    @Inject
    TrainingPlanGeneratorV1 tpGen;

    @Inject
    TrainingGeneratorV1 tgV1;

    @Inject
    TrainingPlanService tpService;

    @Inject
    GoalService goalService;

    @Test
    void testCalculateTrivialWeekRepartition() {
        // Cas 1 : 4 jours disponibles, 2 séances
        List<DayOfWeek> days1 = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
        System.out.println("Cas  1 : " + tpGen.generateTrivialAvailableDays(days1, 2));
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                tpGen.generateTrivialAvailableDays(days1, 2));

        // Cas 2 : tous les jours disponibles, 3 séances
        List<DayOfWeek> days2 = Arrays.asList(DayOfWeek.values());
        System.out.println("Cas  2 : " + tpGen.generateTrivialAvailableDays(days2, 3));
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY),
                tpGen.generateTrivialAvailableDays(days2, 3));

        // Cas 3 : tous les jours disponibles, 4 séances
        System.out.println("Cas  3 : " + tpGen.generateTrivialAvailableDays(days2, 4));
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
                tpGen.generateTrivialAvailableDays(days2, 4));

        // Cas 4 : seulement 3 jours dispos, 2 séances
        List<DayOfWeek> days3 = List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY);
        System.out.println("Cas  4 : " + tpGen.generateTrivialAvailableDays(days3, 2));
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY),
                tpGen.generateTrivialAvailableDays(days3, 2));

        // Cas 5 : 5 jours dispos, 3 séances
        List<DayOfWeek> days4 = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY);
        System.out.println("Cas  5 : " + tpGen.generateTrivialAvailableDays(days4, 3));
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                tpGen.generateTrivialAvailableDays(days4, 3));

        // Cas 6 : tous les jours dispo, 6 séances
        System.out.println("Cas  6 : " + tpGen.generateTrivialAvailableDays(days2, 6));
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                tpGen.generateTrivialAvailableDays(days2, 6));

        // Cas 7 : un seul jour dispo
        List<DayOfWeek> days5 = List.of(DayOfWeek.WEDNESDAY);
        System.out.println("Cas  7 : " + tpGen.generateTrivialAvailableDays(days5, 1));
        assertEquals(List.of(DayOfWeek.WEDNESDAY),
                tpGen.generateTrivialAvailableDays(days5, 1));

        // Cas 8 : 2 jours dispo, 2 séances
        List<DayOfWeek> days6 = List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY);
        System.out.println("Cas  8 : " + tpGen.generateTrivialAvailableDays(days6, 2));
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
                tpGen.generateTrivialAvailableDays(days6, 2));

        // Cas 9 : 7 jours dispo, 7 séances
        System.out.println("Cas  9 : " + tpGen.generateTrivialAvailableDays(days2, 7));
        assertEquals(Arrays.asList(DayOfWeek.values()),
                tpGen.generateTrivialAvailableDays(days2, 7));

        // Cas 10 : 7 jours dispo, 1 séance
        System.out.println("Cas 10 : " + tpGen.generateTrivialAvailableDays(days2, 1));
        assertEquals(List.of(DayOfWeek.MONDAY),
                tpGen.generateTrivialAvailableDays(days2, 1));
    }

    @Test
    void testTrivialTPGenerator() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("runner@etik.com", "Test", "Runner",
                LocalDate.of(1990, 1, 1), 70.0, 175.0, 180);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 70));

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(endDate, days, List.of(running), true);
        TrainingPlan tp = tpGen.generate(dto, account);

        System.out.println(tp);
    }

    @Test
    void testComplexTPGenerator() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 40.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        LocalDate endDate = LocalDate.of(2025, 12, 24);

        Account account = new Account("runner@etik.com", "Test", "Runner",
                LocalDate.of(1990, 1, 1), 70.0, 175.0, 180);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 70));

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(endDate, days, List.of(running, cycling), true);
        TrainingPlan tp = tpGen.generate(dto, account);

        System.out.println(tp);
    }

    @Test
    void testThreeSportBalancedDistribution() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 20.0);
        Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 1.0);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

        Account account = new Account("triathlete@etik.com", "Test", "Athlete",
                LocalDate.of(1995, 6, 15), 72.0, 178.0, 185);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 90));

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(LocalDate.of(2025, 12, 24), days, List.of(running, cycling, swimming), true);
        TrainingPlan tp = tpGen.generate(dto, account);

        System.out.println(tp);
    }

    @Test
    void testTwoSportBalancedDistribution() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 1.0);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

        Account account = new Account("kevin@etik.com", "Test", "Athlete",
                LocalDate.of(1995, 6, 15), 72.0, 178.0, 185);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 90));

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(LocalDate.of(2025, 12, 24), days, List.of(running, swimming), true);
        TrainingPlan tp = tpGen.generate(dto, account);

        System.out.println(tp);
    }

    @Test
    void testTwoSportOnMultipleWorkoutPerDay() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 42.2);
        Goal cycling = goalService.getSpecificGoal(Sport.SWIMMING, 3.9);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

        Account account = new Account("triathlete@etik.com", "Test", "Athlete",
                LocalDate.of(1995, 6, 15), 72.0, 178.0, 185);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 90));

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(LocalDate.of(2025, 12, 24), days, List.of(running, cycling), true);
        TrainingPlan tp = tpGen.generate(dto, account);

        System.out.println(tp);
    }

    @Test
    void testThreeSportOnMultipleWorkoutPerDay() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 42.2);
        Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 180.0);
        Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 3.9);

        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

        Account account = new Account("test@etik.com", "Test", "Athlete",
                LocalDate.of(1995, 6, 15), 72.0, 178.0, 185);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 90));

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(LocalDate.of(2025, 12, 24), days, List.of(running, cycling, swimming), true);
        TrainingPlan tp = tpGen.generate(dto, account);

        System.out.println(tp);
    }
}
