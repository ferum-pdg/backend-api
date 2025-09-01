package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.FitnessLevel;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_generator.generator_V1.TrainingPlanGeneratorV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("Training Plan Generator Tests")
public class TrainingPlanGeneratorTest {

    private static final Logger log = LoggerFactory.getLogger(TrainingPlanGeneratorTest.class);

    @Inject
    TrainingPlanGeneratorV1 tpGen;

    @Inject
    TrainingPlanService tpService;

    @Inject
    GoalService goalService;

    // Test Data Factory Methods
    private Account createTestAccount(String email, String name, int fitnessLevel, LocalDate birthDate) {
        Account account = new Account(email, "Test", name, birthDate, 70.0, 175.0, 180);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), fitnessLevel));
        return account;
    }

    private Account createLowFitnessAccount() {
        return createTestAccount("low@test.com", "LowFitness", 30, LocalDate.of(1990, 1, 1));
    }

    private Account createMediumFitnessAccount() {
        return createTestAccount("medium@test.com", "MediumFitness", 50, LocalDate.of(1990, 1, 1));
    }

    private Account createHighFitnessAccount() {
        return createTestAccount("high@test.com", "HighFitness", 90, LocalDate.of(1990, 1, 1));
    }

    private LocalDate computeEndDate(List<Goal> goals) {
        int totalWeeks = goals.stream().mapToInt(Goal::getNbOfWeek).sum();
        return LocalDate.now().plusWeeks(totalWeeks);
    }

    private TrainingPlanRequestDto createRequestDto(LocalDate endDate, List<DayOfWeek> days, List<Goal> goals) {
        return new TrainingPlanRequestDto(endDate, days, goals, true);
    }

    @Nested
    @DisplayName("Trivial Available Days Generation")
    class TrivialAvailableDaysTests {

        @Test
        @DisplayName("Should distribute workouts evenly across available days")
        void testEvenDistribution() {
            // 4 jours disponibles, 2 séances
            List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
            assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                    tpGen.generateTrivialAvailableDays(days, 2));

            // 7 jours disponibles, 3 séances
            List<DayOfWeek> allDays = Arrays.asList(DayOfWeek.values());
            assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY),
                    tpGen.generateTrivialAvailableDays(allDays, 3));
        }

        @Test
        @DisplayName("Should handle edge cases")
        void testEdgeCases() {
            List<DayOfWeek> allDays = Arrays.asList(DayOfWeek.values());

            // Un seul jour demandé
            assertEquals(List.of(DayOfWeek.MONDAY),
                    tpGen.generateTrivialAvailableDays(allDays, 1));

            // Tous les jours demandés
            assertEquals(Arrays.asList(DayOfWeek.values()),
                    tpGen.generateTrivialAvailableDays(allDays, 7));

            // Un seul jour disponible
            List<DayOfWeek> singleDay = List.of(DayOfWeek.WEDNESDAY);
            assertEquals(List.of(DayOfWeek.WEDNESDAY),
                    tpGen.generateTrivialAvailableDays(singleDay, 1));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7})
        @DisplayName("Should never return more days than requested")
        void testNeverExceedsRequestedDays(int nbWorkouts) {
            List<DayOfWeek> allDays = Arrays.asList(DayOfWeek.values());
            List<DayOfWeek> result = tpGen.generateTrivialAvailableDays(allDays, nbWorkouts);
            assertEquals(nbWorkouts, result.size());
        }
    }

    @Nested
    @DisplayName("Training Plan Generation - Valid Cases")
    class ValidTrainingPlanTests {

        @Test
        @DisplayName("Should generate simple single sport plan")
        void testSimpleSingleSport() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
            List<Goal> goals = List.of(running);
            List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
            Account account = createMediumFitnessAccount();

            TrainingPlanRequestDto dto = createRequestDto(computeEndDate(goals), days, goals);
            TrainingPlan tp = tpGen.generate(dto, account);

            assertNotNull(tp);
            assertFalse(tp.getWeeklyPlans().isEmpty());
        }

        @Test
        @DisplayName("Should generate complex multi-sport plan")
        void testMultiSportPlan() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
            Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 40.0);
            List<Goal> goals = List.of(running, cycling);
            List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
            Account account = createMediumFitnessAccount();

            TrainingPlanRequestDto dto = createRequestDto(computeEndDate(goals), days, goals);
            TrainingPlan tp = tpGen.generate(dto, account);

            assertNotNull(tp);
            assertEquals(goals, tp.getGoals());
        }

        @Test
        @DisplayName("Should generate triathlon plan with balanced distribution")
        void testTriathlonBalancedDistribution() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
            Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 20.0);
            Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 1.0);
            List<Goal> goals = List.of(running, cycling, swimming);
            List<DayOfWeek> days = Arrays.asList(DayOfWeek.values());
            Account account = createHighFitnessAccount();

            TrainingPlanRequestDto dto = createRequestDto(computeEndDate(goals), days, goals);
            TrainingPlan tp = tpGen.generate(dto, account);

            assertNotNull(tp);
            assertEquals(3, tp.getGoals().size());
        }

        @Test
        @DisplayName("Should handle multiple workouts per day for high fitness level")
        void testMultipleWorkoutsPerDay() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 42.2);
            Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 3.9);
            List<Goal> goals = List.of(running, swimming);
            List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
            Account account = createHighFitnessAccount();

            TrainingPlanRequestDto dto = createRequestDto(computeEndDate(goals), days, goals);
            TrainingPlan tp = tpGen.generate(dto, account);

            assertNotNull(tp);
        }
    }

    @Nested
    @DisplayName("Training Plan Generation - Exception Cases")
    class ExceptionTrainingPlanTests {

        @Test
        @DisplayName("Should throw exception when no goals provided")
        void testNoGoalsException() {
            List<Goal> emptyGoals = List.of();
            List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
            Account account = createMediumFitnessAccount();

            TrainingPlanRequestDto dto = createRequestDto(LocalDate.now().plusWeeks(10), days, emptyGoals);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                tpGen.generate(dto, account);
            });

            assertTrue(exception.getMessage().contains("No available goals"));
        }

        @Test
        @DisplayName("Should throw exception when end date is too soon")
        void testEndDateTooSoonException() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 42.2);
            List<Goal> goals = List.of(running);
            List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
            Account account = createMediumFitnessAccount();

            // End date too soon (tomorrow instead of required weeks)
            LocalDate tooSoonEndDate = LocalDate.now().plusDays(1);
            TrainingPlanRequestDto dto = createRequestDto(tooSoonEndDate, days, goals);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                tpGen.generate(dto, account);
            });

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when not enough available days")
        void testNotEnoughAvailableDaysException() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 42.2);
            Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 180.0);
            Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 3.9);
            List<Goal> goals = List.of(running, cycling, swimming);

            // Only 2 days available but need more workouts
            List<DayOfWeek> fewDays = List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
            Account account = createLowFitnessAccount(); // Low fitness = no multiple workouts per day

            TrainingPlanRequestDto dto = createRequestDto(computeEndDate(goals), fewDays, goals);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                tpGen.generate(dto, account);
            });

            assertTrue(exception.getMessage().contains("Not enough available days"));
        }

        @Test
        @DisplayName("Should throw exception for intensive goals with insufficient days")
        void testIntensiveGoalsInsufficientDaysException() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 42.2);
            Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 180.0);
            Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 3.9);
            List<Goal> goals = List.of(running, cycling, swimming);

            List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
            Account account = createHighFitnessAccount();

            TrainingPlanRequestDto dto = createRequestDto(computeEndDate(goals), days, goals);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                tpGen.generate(dto, account);
            });

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for medium goals with low fitness")
        void testMediumGoalsLowFitnessException() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 21.1);
            Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 100.0);
            List<Goal> goals = List.of(running, cycling);

            List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
            Account account = createTestAccount("test@test.com", "Test", 33, LocalDate.of(1995, 6, 15));

            TrainingPlanRequestDto dto = createRequestDto(computeEndDate(goals), days, goals);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                tpGen.generate(dto, account);
            });

            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for multiple workouts without swimming")
        void testMultipleWorkoutsWithoutSwimmingException() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 42.2);
            Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 180.0);
            List<Goal> goals = List.of(running, cycling); // No swimming

            List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY); // Few days to force multiple workouts per day
            Account account = createHighFitnessAccount();

            TrainingPlanRequestDto dto = createRequestDto(computeEndDate(goals), days, goals);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                tpGen.generate(dto, account);
            });

            assertNotNull(exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Calculation Methods Tests")
    class CalculationTests {

        @Test
        @DisplayName("Should calculate correct number of weeks")
        void testCalculateNbWeeksOfTraining() {
            Goal shortGoal = goalService.getSpecificGoal(Sport.RUNNING, 5.0);
            Goal longGoal = goalService.getSpecificGoal(Sport.CYCLING, 100.0);
            List<Goal> goals = List.of(shortGoal, longGoal);

            int result = tpGen.calculateNbWeeksOfTraining(goals);

            assertTrue(result > 0);
            // Should return the maximum number of weeks from all goals
        }

        @Test
        @DisplayName("Should calculate correct number of workouts per week")
        void testCalculateNbOfWorkoutsPerWeek() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
            Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 20.0);
            List<Goal> goals = List.of(running, cycling);

            int result = tpGen.calculateNbOfWorkoutsPerWeek(goals);

            assertTrue(result > 0);
            // Should return the sum of workouts per week from all goals
        }

        @Test
        @DisplayName("Should authorize multiple workouts per day correctly")
        void testAuthorizeMultipleWorkoutsPerDay() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 21.1);
            Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 1.8);
            List<Goal> goals = List.of(running, swimming);

            // High fitness level should authorize multiple workouts
            assertTrue(tpGen.authorizeMultipleWorkoutsPerDay(goals, 10, 70, 5));

            // Low fitness level should not authorize multiple workouts
            assertFalse(tpGen.authorizeMultipleWorkoutsPerDay(goals, 10, 50, 5));

            // No swimming should not authorize multiple workouts
            Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 20.0);
            List<Goal> noSwimmingGoals = List.of(running, cycling);
            assertFalse(tpGen.authorizeMultipleWorkoutsPerDay(noSwimmingGoals, 10, 70, 5));
        }
    }

    @Nested
    @DisplayName("Boundary Value Tests")
    class BoundaryValueTests {

        @Test
        @DisplayName("Should handle minimum fitness level boundary")
        void testMinimumFitnessLevelBoundary() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
            Goal swimming = goalService.getSpecificGoal(Sport.SWIMMING, 1.0);
            List<Goal> goals = List.of(running, swimming);

            // Test exactly at boundary (65)
            assertTrue(tpGen.authorizeMultipleWorkoutsPerDay(goals, 8, 65, 5));

            // Test just below boundary (64)
            assertFalse(tpGen.authorizeMultipleWorkoutsPerDay(goals, 8, 64, 5));
        }

        @Test
        @DisplayName("Should handle exact number of days vs workouts")
        void testExactDaysVsWorkouts() {
            Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
            Goal cycling = goalService.getSpecificGoal(Sport.CYCLING, 20.0);
            List<Goal> goals = List.of(running, cycling);

            // Exactly matching days and workouts
            List<DayOfWeek> exactDays = List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
            Account account = createMediumFitnessAccount();

            TrainingPlanRequestDto dto = createRequestDto(computeEndDate(goals), exactDays, goals);

            // Should not throw exception
            assertDoesNotThrow(() -> tpGen.generate(dto, account));
        }
    }
}