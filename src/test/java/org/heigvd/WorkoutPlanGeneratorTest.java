package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.entity.workout.IntensityZone;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.entity.workout.details.WorkoutPlan;
import org.heigvd.entity.workout.details.WorkoutPlanDetails;
import org.heigvd.training_generator.generator_V2.WorkoutPlanGeneratorV2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("Workout Plan Generator V2 Tests")
public class WorkoutPlanGeneratorTest {

    @Inject
    WorkoutPlanGeneratorV2 wpGen;

    // Constants pour les tests
    private static final int LOW_FITNESS = 30;
    private static final int MEDIUM_FITNESS = 50;
    private static final int HIGH_FITNESS = 90;
    private static final double START_PROGRESSION = 0.0;
    private static final double MID_PROGRESSION = 0.5;
    private static final double END_PROGRESSION = 1.0;

    // Helper methods pour la validation
    private void assertWorkoutPlanStructure(List<WorkoutPlan> plans, WorkoutType expectedType) {
        assertNotNull(plans, "Plans should not be null");
        assertFalse(plans.isEmpty(), "Plans should not be empty");

        // Vérifier que tous les plans ont le bon type
        plans.forEach(plan -> {
            assertNotNull(plan.getType());
            assertEquals(expectedType, plan.getType());
            assertNotNull(plan.getDetails());
            assertFalse(plan.getDetails().isEmpty());
        });
    }

    private void assertWorkoutPlanDetails(List<WorkoutPlan> plans) {
        plans.forEach(plan -> {
            assertTrue(plan.getBlocId() > 0, "Bloc ID should be positive");
            assertTrue(plan.getRepetitionCount() > 0, "Repetition count should be positive");

            plan.getDetails().forEach(detail -> {
                assertTrue(detail.getBlocDetailId() > 0, "Detail ID should be positive");
                assertTrue(detail.getDurationSec() >= 30, "Duration should be at least 30 seconds");
                assertNotNull(detail.getIntensityZone(), "Intensity zone should not be null");
            });
        });
    }

    private void assertTotalDurationReasonable(List<WorkoutPlan> plans, int minSeconds, int maxSeconds) {
        int totalDuration = plans.stream()
                .mapToInt(plan -> plan.getDetails().stream()
                        .mapToInt(detail -> detail.getDurationSec() * plan.getRepetitionCount())
                        .sum())
                .sum();

        assertTrue(totalDuration >= minSeconds,
                String.format("Total duration %d should be at least %d seconds", totalDuration, minSeconds));
        assertTrue(totalDuration <= maxSeconds,
                String.format("Total duration %d should not exceed %d seconds", totalDuration, maxSeconds));
    }

    @Nested
    @DisplayName("Endurance Fondamentale (EF) Tests")
    class EnduranceFondamentaleTests {

        @ParameterizedTest
        @EnumSource(Sport.class)
        @DisplayName("Should generate EF workout for all sports")
        void testGenerateEFForAllSports(Sport sport) {
            List<WorkoutPlan> plans = wpGen.generate(sport, WorkoutType.EF, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);

            assertWorkoutPlanStructure(plans, WorkoutType.EF);
            assertWorkoutPlanDetails(plans);

            // EF doit avoir exactement 3 blocs (échauffement, corps, récup)
            assertEquals(3, plans.size(), "EF should have 3 blocks");

            // Vérifier la structure 10-80-10
            List<WorkoutPlanDetails> allDetails = plans.stream()
                    .flatMap(plan -> plan.getDetails().stream())
                    .toList();

            // Premier et dernier bloc doivent être en récupération
            assertEquals(IntensityZone.RECOVERY, allDetails.get(0).getIntensityZone());
            assertEquals(IntensityZone.RECOVERY, allDetails.get(allDetails.size() - 1).getIntensityZone());

            // Bloc principal doit être en endurance
            assertEquals(IntensityZone.ENDURANCE, plans.get(1).getDetails().get(0).getIntensityZone());
        }

        @ParameterizedTest
        @ValueSource(ints = {20, 50, 80})
        @DisplayName("Should scale duration with fitness level")
        void testEFScalesWithFitnessLevel(int fitnessLevel) {
            List<WorkoutPlan> lowFitness = wpGen.generate(Sport.RUNNING, WorkoutType.EF, 20, MID_PROGRESSION, TrainingPlanPhase.BASE);
            List<WorkoutPlan> highFitness = wpGen.generate(Sport.RUNNING, WorkoutType.EF, 80, MID_PROGRESSION, TrainingPlanPhase.BASE);

            int lowDuration = calculateTotalDuration(lowFitness);
            int highDuration = calculateTotalDuration(highFitness);

            assertTrue(highDuration > lowDuration, "Higher fitness should result in longer duration");
        }

        @ParameterizedTest
        @EnumSource(TrainingPlanPhase.class)
        @DisplayName("Should adapt to training phases")
        void testEFAdaptsToPhases(TrainingPlanPhase phase) {
            List<WorkoutPlan> plans = wpGen.generate(Sport.RUNNING, WorkoutType.EF, MEDIUM_FITNESS, MID_PROGRESSION, phase);

            assertWorkoutPlanStructure(plans, WorkoutType.EF);

            // Phase BASE devrait avoir des durées plus longues que SHARPENING
            if (phase == TrainingPlanPhase.BASE) {
                assertTotalDurationReasonable(plans, 1800, 8000); // 30min à 2h20
            } else if (phase == TrainingPlanPhase.SHARPENING) {
                assertTotalDurationReasonable(plans, 1200, 6000); // 20min à 1h40
            }
        }
    }

    @Nested
    @DisplayName("Interval Training Tests")
    class IntervalTrainingTests {

        @ParameterizedTest
        @EnumSource(Sport.class)
        @DisplayName("Should generate interval workout for all sports")
        void testGenerateIntervalForAllSports(Sport sport) {
            List<WorkoutPlan> plans = wpGen.generate(sport, WorkoutType.INTERVAL, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.SPECIFIC);

            assertWorkoutPlanStructure(plans, WorkoutType.INTERVAL);
            assertWorkoutPlanDetails(plans);

            // Interval doit avoir 3 blocs (warm-up, intervals, cool-down)
            assertEquals(3, plans.size(), "Interval should have 3 blocks");

            // Le bloc du milieu doit avoir des répétitions > 1
            assertTrue(plans.get(1).getRepetitionCount() > 1, "Main interval block should have multiple repetitions");

            // Le bloc d'intervalles doit avoir 2 détails (effort + récup)
            assertEquals(2, plans.get(1).getDetails().size(), "Interval block should have effort and recovery segments");
        }

        @ParameterizedTest
        @EnumSource(TrainingPlanPhase.class)
        @DisplayName("Should adapt interval intensity to training phase")
        void testIntervalIntensityAdaptation(TrainingPlanPhase phase) {
            List<WorkoutPlan> plans = wpGen.generate(Sport.RUNNING, WorkoutType.INTERVAL, HIGH_FITNESS, MID_PROGRESSION, phase);

            WorkoutPlan intervalBlock = plans.get(1); // Bloc principal
            IntensityZone effortZone = intervalBlock.getDetails().get(0).getIntensityZone();

            switch (phase) {
                case BASE -> assertEquals(IntensityZone.TEMPO, effortZone, "BASE phase should use TEMPO intensity");
                case SPECIFIC -> assertEquals(IntensityZone.THRESHOLD, effortZone, "SPECIFIC phase should use THRESHOLD intensity");
                case SHARPENING -> assertEquals(IntensityZone.VO2_MAX, effortZone, "SHARPENING phase should use VO2_MAX intensity");
            }
        }

        @Test
        @DisplayName("Should have shorter intervals for swimming")
        void testSwimmingIntervalDuration() {
            List<WorkoutPlan> runningPlans = wpGen.generate(Sport.RUNNING, WorkoutType.INTERVAL, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.SPECIFIC);
            List<WorkoutPlan> swimmingPlans = wpGen.generate(Sport.SWIMMING, WorkoutType.INTERVAL, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.SPECIFIC);

            int runningEffortDuration = runningPlans.get(1).getDetails().get(0).getDurationSec();
            int swimmingEffortDuration = swimmingPlans.get(1).getDetails().get(0).getDurationSec();

            assertTrue(swimmingEffortDuration <= runningEffortDuration,
                    "Swimming intervals should be shorter or equal to running intervals");
        }
    }

    @Nested
    @DisplayName("Lactate Threshold Tests")
    class LactateThresholdTests {

        @ParameterizedTest
        @EnumSource(Sport.class)
        @DisplayName("Should generate lactate workout for all sports")
        void testGenerateLactateForAllSports(Sport sport) {
            List<WorkoutPlan> plans = wpGen.generate(sport, WorkoutType.LACTATE, HIGH_FITNESS, MID_PROGRESSION, TrainingPlanPhase.SPECIFIC);

            assertWorkoutPlanStructure(plans, WorkoutType.LACTATE);
            assertWorkoutPlanDetails(plans);

            assertEquals(3, plans.size(), "Lactate should have 3 blocks");

            // Le bloc principal doit utiliser l'intensité THRESHOLD
            assertEquals(IntensityZone.THRESHOLD, plans.get(1).getDetails().get(0).getIntensityZone(),
                    "Lactate training should use THRESHOLD intensity");
        }

        @Test
        @DisplayName("Should have longer efforts than intervals")
        void testLactateEffortDurationLongerThanIntervals() {
            List<WorkoutPlan> lactatePlans = wpGen.generate(Sport.RUNNING, WorkoutType.LACTATE, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.SPECIFIC);
            List<WorkoutPlan> intervalPlans = wpGen.generate(Sport.RUNNING, WorkoutType.INTERVAL, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.SPECIFIC);

            int lactateEffortDuration = lactatePlans.get(1).getDetails().get(0).getDurationSec();
            int intervalEffortDuration = intervalPlans.get(1).getDetails().get(0).getDurationSec();

            assertTrue(lactateEffortDuration > intervalEffortDuration,
                    "Lactate efforts should be longer than interval efforts");
        }
    }

    @Nested
    @DisplayName("Endurance Active (Tempo) Tests")
    class EnduranceActiveTests {

        @ParameterizedTest
        @EnumSource(Sport.class)
        @DisplayName("Should generate EA workout for all sports")
        void testGenerateEAForAllSports(Sport sport) {
            List<WorkoutPlan> plans = wpGen.generate(sport, WorkoutType.EA, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);

            assertWorkoutPlanStructure(plans, WorkoutType.EA);
            assertWorkoutPlanDetails(plans);

            assertEquals(3, plans.size(), "EA should have 3 blocks");

            // Le bloc principal doit utiliser l'intensité TEMPO
            assertEquals(IntensityZone.TEMPO, plans.get(1).getDetails().get(0).getIntensityZone(),
                    "EA training should use TEMPO intensity");
        }

        @Test
        @DisplayName("Should have reasonable recovery ratio")
        void testEARecoveryRatio() {
            List<WorkoutPlan> plans = wpGen.generate(Sport.RUNNING, WorkoutType.EA, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);

            WorkoutPlan mainBlock = plans.get(1);
            int effortDuration = mainBlock.getDetails().get(0).getDurationSec();
            int recoveryDuration = mainBlock.getDetails().get(1).getDurationSec();

            // La récupération devrait être environ 25% de l'effort
            double recoveryRatio = (double) recoveryDuration / effortDuration;
            assertTrue(recoveryRatio >= 0.2 && recoveryRatio <= 0.3,
                    "EA recovery should be around 25% of effort duration");
        }
    }

    @Nested
    @DisplayName("Technical Training Tests")
    class TechnicalTrainingTests {

        @ParameterizedTest
        @EnumSource(Sport.class)
        @DisplayName("Should generate technical workout for all sports")
        void testGenerateTechnicForAllSports(Sport sport) {
            List<WorkoutPlan> plans = wpGen.generate(sport, WorkoutType.TECHNIC, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);

            assertWorkoutPlanStructure(plans, WorkoutType.TECHNIC);
            assertWorkoutPlanDetails(plans);

            // Tout le travail technique doit être en récupération
            plans.forEach(plan -> {
                plan.getDetails().forEach(detail -> {
                    assertEquals(IntensityZone.RECOVERY, detail.getIntensityZone(),
                            "Technical training should use RECOVERY intensity");
                });
            });
        }

        @Test
        @DisplayName("Should have different structure for swimming vs other sports")
        void testSwimmingTechnicDifferentStructure() {
            List<WorkoutPlan> swimmingPlans = wpGen.generate(Sport.SWIMMING, WorkoutType.TECHNIC, HIGH_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);
            List<WorkoutPlan> runningPlans = wpGen.generate(Sport.RUNNING, WorkoutType.TECHNIC, HIGH_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);

            // La natation devrait avoir plus de segments dans le bloc principal
            int swimmingMainSegments = swimmingPlans.get(1).getDetails().size();
            int runningMainSegments = runningPlans.get(1).getDetails().size();

            assertTrue(swimmingMainSegments >= runningMainSegments,
                    "Swimming technique should have more varied segments");
        }
    }

    @Nested
    @DisplayName("Recovery Active Tests")
    class RecoveryActiveTests {

        @ParameterizedTest
        @EnumSource(Sport.class)
        @DisplayName("Should generate RA workout for all sports")
        void testGenerateRAForAllSports(Sport sport) {
            List<WorkoutPlan> plans = wpGen.generate(sport, WorkoutType.RA, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);

            assertWorkoutPlanStructure(plans, WorkoutType.RA);
            assertWorkoutPlanDetails(plans);

            // RA devrait n'avoir qu'un seul bloc
            assertEquals(1, plans.size(), "RA should have only 1 block");

            // Tout doit être en récupération
            assertEquals(IntensityZone.RECOVERY, plans.get(0).getDetails().get(0).getIntensityZone(),
                    "RA should use RECOVERY intensity");
        }

        @Test
        @DisplayName("Should be shortest workout type")
        void testRAShouldBeShortest() {
            List<WorkoutPlan> raPlans = wpGen.generate(Sport.RUNNING, WorkoutType.RA, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);
            List<WorkoutPlan> efPlans = wpGen.generate(Sport.RUNNING, WorkoutType.EF, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);

            int raDuration = calculateTotalDuration(raPlans);
            int efDuration = calculateTotalDuration(efPlans);

            assertTrue(raDuration < efDuration, "RA should be shorter than EF");
        }
    }

    @Nested
    @DisplayName("Progression and Scaling Tests")
    class ProgressionScalingTests {

        @ParameterizedTest
        @CsvSource({
                "0.0, 1.0, RUNNING, INTERVAL",
                "0.5, 1.0, CYCLING, LACTATE",
                "1.0, 0.0, SWIMMING, EA"
        })
        @DisplayName("Should scale with progression")
        void testProgressionScaling(double highProgression, double lowProgression, Sport sport, WorkoutType type) {
            List<WorkoutPlan> highProgressionPlans = wpGen.generate(sport, type, MEDIUM_FITNESS, highProgression, TrainingPlanPhase.BASE);
            List<WorkoutPlan> lowProgressionPlans = wpGen.generate(sport, type, MEDIUM_FITNESS, lowProgression, TrainingPlanPhase.BASE);

            int highDuration = calculateTotalDuration(highProgressionPlans);
            int lowDuration = calculateTotalDuration(lowProgressionPlans);

            // La progression devrait influencer la durée ou l'intensité
            assertNotEquals(highDuration, lowDuration, "Different progressions should yield different results");
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 25, 50, 75, 100})
        @DisplayName("Should handle all fitness level boundaries")
        void testFitnessLevelBoundaries(int fitnessLevel) {
            assertDoesNotThrow(() -> {
                List<WorkoutPlan> plans = wpGen.generate(Sport.RUNNING, WorkoutType.INTERVAL, fitnessLevel, MID_PROGRESSION, TrainingPlanPhase.BASE);
                assertWorkoutPlanStructure(plans, WorkoutType.INTERVAL);
            }, "Should handle fitness level " + fitnessLevel);
        }

        @Test
        @DisplayName("Should scale cycling durations appropriately")
        void testCyclingDurationScaling() {
            List<WorkoutPlan> runningPlans = wpGen.generate(Sport.RUNNING, WorkoutType.EF, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);
            List<WorkoutPlan> cyclingPlans = wpGen.generate(Sport.CYCLING, WorkoutType.EF, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);

            int runningDuration = calculateTotalDuration(runningPlans);
            int cyclingDuration = calculateTotalDuration(cyclingPlans);

            assertTrue(cyclingDuration > runningDuration, "Cycling workouts should be longer than running");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should throw exception for unsupported workout type")
        void testUnsupportedWorkoutType() {
            // Assuming there's a workout type that's not supported
            assertThrows(IllegalArgumentException.class, () -> {
                wpGen.generate(Sport.RUNNING, null, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);
            });
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.1, 1.1, 2.0})
        @DisplayName("Should handle invalid progression values gracefully")
        void testInvalidProgressionValues(double invalidProgression) {
            // Should not throw but should clamp or handle gracefully
            assertDoesNotThrow(() -> {
                List<WorkoutPlan> plans = wpGen.generate(Sport.RUNNING, WorkoutType.EF, MEDIUM_FITNESS, invalidProgression, TrainingPlanPhase.BASE);
                assertWorkoutPlanStructure(plans, WorkoutType.EF);
            });
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -10, 101, 150})
        @DisplayName("Should handle extreme fitness levels")
        void testExtremeFitnessLevels(int extremeLevel) {
            // Should handle gracefully without throwing
            assertDoesNotThrow(() -> {
                List<WorkoutPlan> plans = wpGen.generate(Sport.RUNNING, WorkoutType.EF, extremeLevel, MID_PROGRESSION, TrainingPlanPhase.BASE);
                assertWorkoutPlanStructure(plans, WorkoutType.EF);

                // Should enforce minimum duration
                plans.forEach(plan -> {
                    plan.getDetails().forEach(detail -> {
                        assertTrue(detail.getDurationSec() >= 30, "Should enforce minimum 30 second duration");
                    });
                });
            });
        }

        @Test
        @DisplayName("Should ensure minimum workout duration")
        void testMinimumWorkoutDuration() {
            // Test avec fitness très bas
            List<WorkoutPlan> plans = wpGen.generate(Sport.RUNNING, WorkoutType.RA, 1, START_PROGRESSION, TrainingPlanPhase.SHARPENING);

            int totalDuration = calculateTotalDuration(plans);
            assertTrue(totalDuration >= 300, "Minimum workout should be at least 5 minutes"); // 300 seconds = 5 minutes
        }
    }

    @Nested
    @DisplayName("Integration and Consistency Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should maintain consistency across multiple generations")
        void testConsistencyAcrossGenerations() {
            List<WorkoutPlan> firstGeneration = wpGen.generate(Sport.RUNNING, WorkoutType.INTERVAL, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);
            List<WorkoutPlan> secondGeneration = wpGen.generate(Sport.RUNNING, WorkoutType.INTERVAL, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.BASE);

            assertEquals(firstGeneration.size(), secondGeneration.size(), "Should generate same number of blocks");

            for (int i = 0; i < firstGeneration.size(); i++) {
                WorkoutPlan first = firstGeneration.get(i);
                WorkoutPlan second = secondGeneration.get(i);

                assertEquals(first.getRepetitionCount(), second.getRepetitionCount(), "Repetitions should be consistent");
                assertEquals(first.getDetails().size(), second.getDetails().size(), "Number of details should be consistent");
            }
        }

        @Test
        @DisplayName("Should generate realistic workout combinations")
        void testRealisticWorkoutCombinations() {
            // Test une séquence complète d'entraînements
            WorkoutType[] weeklyProgram = {WorkoutType.EF, WorkoutType.INTERVAL, WorkoutType.EA, WorkoutType.LACTATE, WorkoutType.TECHNIC, WorkoutType.RA};

            for (WorkoutType type : weeklyProgram) {
                List<WorkoutPlan> plans = wpGen.generate(Sport.RUNNING, type, MEDIUM_FITNESS, MID_PROGRESSION, TrainingPlanPhase.SPECIFIC);
                assertWorkoutPlanStructure(plans, type);

                int totalDuration = calculateTotalDuration(plans);
                assertTrue(totalDuration >= 600 && totalDuration <= 7200, // 10 min à 2h
                        String.format("Workout %s duration %d should be realistic", type, totalDuration));
            }
        }
    }

    // Utility methods
    private int calculateTotalDuration(List<WorkoutPlan> plans) {
        return plans.stream()
                .mapToInt(plan -> plan.getDetails().stream()
                        .mapToInt(detail -> detail.getDurationSec() * plan.getRepetitionCount())
                        .sum())
                .sum();
    }
}