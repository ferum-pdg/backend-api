package org.heigvd.training_generator.generator_V2;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.Sport;
import org.heigvd.entity.workout.*;
import org.heigvd.entity.workout.details.*;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.training_generator.interfaces.WorkoutPlanGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Advanced version 2 implementation of the workout plan generator.
 * This implementation creates detailed, adaptive workout plans with:
 *
 * - Sport-specific workout structures
 * - Phase-aware intensity progression
 * - Dynamic duration calculations based on multiple factors
 * - Intelligent parameter adaptation for intervals and threshold work
 * - Specialized technical training protocols
 *
 * The generator follows established sports science principles for
 * endurance training periodization and adaptation.
 *
 * @version 2.0
 */
@ApplicationScoped
public class WorkoutPlanGeneratorV2 implements WorkoutPlanGenerator {

    /**
     * Centralized configuration for base workout durations by sport and type.
     * All durations are in seconds and serve as the foundation for dynamic calculations.
     */
    private static final Map<Sport, Map<WorkoutType, Integer>> BASE_DURATIONS = Map.of(
            Sport.RUNNING, Map.of(
                    WorkoutType.EF, 2700,      // 45 minutes
                    WorkoutType.INTERVAL, 2400, // 40 minutes
                    WorkoutType.LACTATE, 3000,  // 50 minutes
                    WorkoutType.EA, 2100,       // 35 minutes
                    WorkoutType.TECHNIC, 1500,  // 25 minutes
                    WorkoutType.RA, 1200        // 20 minutes
            ),
            Sport.CYCLING, Map.of(
                    WorkoutType.EF, 5400,       // 90 minutes
                    WorkoutType.INTERVAL, 3600, // 60 minutes
                    WorkoutType.LACTATE, 4200,  // 70 minutes
                    WorkoutType.EA, 2700,       // 45 minutes
                    WorkoutType.TECHNIC, 1800,  // 30 minutes
                    WorkoutType.RA, 2400        // 40 minutes
            ),
            Sport.SWIMMING, Map.of(
                    WorkoutType.EF, 2400,       // 40 minutes
                    WorkoutType.INTERVAL, 1800, // 30 minutes
                    WorkoutType.LACTATE, 2100,  // 35 minutes
                    WorkoutType.EA, 1800,       // 30 minutes
                    WorkoutType.TECHNIC, 1500,  // 25 minutes
                    WorkoutType.RA, 1200        // 20 minutes
            )
    );

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return "V2";
    }

    /**
     * Generates a complete workout plan with adaptive parameters based on
     * sport type, training phase, fitness level, and progression.
     *
     * This method creates structured workouts that adapt to the athlete's
     * development and follow established periodization principles.
     *
     * @param sport the sport for the workout
     * @param workoutType the type of workout to generate
     * @param fitnessLevel the athlete's fitness level (1-100)
     * @param progressionPercent the progression through the training plan (0.0-1.0)
     * @param phase the current training phase
     * @return a list of workout plan blocks
     * @throws IllegalArgumentException if the workout type is null or unsupported
     */
    @Override
    public List<WorkoutPlan> generate(Sport sport, WorkoutType workoutType, int fitnessLevel,
                                      double progressionPercent, TrainingPlanPhase phase) {

        if (workoutType == null) {
            throw new IllegalArgumentException("Workout type cannot be null");
        }

        return switch (workoutType) {
            case EF -> generateEnduranceFundamental(sport, fitnessLevel, progressionPercent, phase);
            case EA -> generateEnduranceActive(sport, fitnessLevel, progressionPercent, phase);
            case LACTATE -> generateLactate(sport, fitnessLevel, progressionPercent, phase);
            case INTERVAL -> generateInterval(sport, fitnessLevel, progressionPercent, phase);
            case TECHNIC -> generateTechnic(sport, fitnessLevel, progressionPercent, phase);
            case RA -> generateRecuperationActive(sport, fitnessLevel, progressionPercent, phase);
            default -> throw new IllegalArgumentException("Unsupported workout type: " + workoutType);
        };
    }

    /**
     * Generates a fundamental endurance workout with the classic 10-80-10% structure.
     * This workout type forms the aerobic base and follows a simple warm-up,
     * main set, and cool-down pattern at steady endurance intensity.
     *
     * @param sport the sport being trained
     * @param level the athlete's fitness level (1-100)
     * @param progression the progression through the training plan (0.0-1.0)
     * @param phase the current training phase
     * @return a structured endurance workout plan
     */
    private List<WorkoutPlan> generateEnduranceFundamental(Sport sport, int level,
                                                           double progression, TrainingPlanPhase phase) {

        int totalDuration = calculateTotalDuration(sport, WorkoutType.EF, level, progression, phase);

        return List.of(
                createWorkoutPlan(1, 1, WorkoutType.EF, List.of(
                        createSegment(1, (int)(totalDuration * 0.1), IntensityZone.RECOVERY)
                )),
                createWorkoutPlan(2, 1, WorkoutType.EF, List.of(
                        createSegment(1, (int)(totalDuration * 0.8), IntensityZone.ENDURANCE)
                )),
                createWorkoutPlan(3, 1, WorkoutType.EF, List.of(
                        createSegment(1, (int)(totalDuration * 0.1), IntensityZone.RECOVERY)
                ))
        );
    }

    /**
     * Generates adaptive interval training with parameters that adjust based on
     * training phase, fitness level, and sport requirements.
     *
     * The structure adapts from longer, tempo-based intervals in the base phase
     * to shorter, high-intensity intervals during the sharpening phase.
     *
     * @param sport the sport being trained
     * @param level the athlete's fitness level (1-100)
     * @param progression the progression through the training plan (0.0-1.0)
     * @param phase the current training phase
     * @return a structured interval workout plan
     */
    private List<WorkoutPlan> generateInterval(Sport sport, int level, double progression, TrainingPlanPhase phase) {

        int totalDuration = calculateTotalDuration(sport, WorkoutType.INTERVAL, level, progression, phase);
        IntervalParams params = calculateIntervalParams(phase, level, sport, progression);

        int warmupDuration = (int)(totalDuration * 0.25);
        int mainWorkDuration = (params.effortDuration + params.recoveryDuration) * params.repetitions;
        int cooldownDuration = Math.max(300, totalDuration - warmupDuration - mainWorkDuration);

        return List.of(
                createWorkoutPlan(1, 1, WorkoutType.INTERVAL, List.of(
                        createSegment(1, warmupDuration, IntensityZone.ENDURANCE)
                )),
                createWorkoutPlan(2, params.repetitions, WorkoutType.INTERVAL, List.of(
                        createSegment(1, params.effortDuration, params.effortZone),
                        createSegment(2, params.recoveryDuration, IntensityZone.ENDURANCE)
                )),
                createWorkoutPlan(3, 1, WorkoutType.INTERVAL, List.of(
                        createSegment(1, cooldownDuration, IntensityZone.RECOVERY)
                ))
        );
    }

    /**
     * Generates lactate threshold training with parameters adapted to the
     * athlete's development level and training phase.
     *
     * This workout type targets the lactate threshold with sustained efforts
     * that progressively increase in intensity and decrease in recovery ratio
     * as the athlete advances through the training phases.
     *
     * @param sport the sport being trained
     * @param level the athlete's fitness level (1-100)
     * @param progression the progression through the training plan (0.0-1.0)
     * @param phase the current training phase
     * @return a structured lactate threshold workout plan
     */
    private List<WorkoutPlan> generateLactate(Sport sport, int level, double progression, TrainingPlanPhase phase) {

        int totalDuration = calculateTotalDuration(sport, WorkoutType.LACTATE, level, progression, phase);
        LactateParams params = calculateLactateParams(phase, level, sport, progression);

        int warmupDuration = (int)(totalDuration * 0.2);
        int mainWorkDuration = (params.effortDuration + params.recoveryDuration) * params.repetitions;
        int cooldownDuration = Math.max(300, totalDuration - warmupDuration - mainWorkDuration);

        return List.of(
                createWorkoutPlan(1, 1, WorkoutType.LACTATE, List.of(
                        createSegment(1, warmupDuration, IntensityZone.ENDURANCE)
                )),
                createWorkoutPlan(2, params.repetitions, WorkoutType.LACTATE, List.of(
                        createSegment(1, params.effortDuration, IntensityZone.THRESHOLD),
                        createSegment(2, params.recoveryDuration, IntensityZone.ENDURANCE)
                )),
                createWorkoutPlan(3, 1, WorkoutType.LACTATE, List.of(
                        createSegment(1, cooldownDuration, IntensityZone.RECOVERY)
                ))
        );
    }

    /**
     * Generates active endurance (tempo) training with adaptive parameters.
     * This workout type builds aerobic power through sustained tempo efforts
     * with parameters that adjust based on fitness level and progression.
     *
     * @param sport the sport being trained
     * @param level the athlete's fitness level (1-100)
     * @param progression the progression through the training plan (0.0-1.0)
     * @param phase the current training phase
     * @return a structured active endurance workout plan
     */
    private List<WorkoutPlan> generateEnduranceActive(Sport sport, int level, double progression, TrainingPlanPhase phase) {

        int totalDuration = calculateTotalDuration(sport, WorkoutType.EA, level, progression, phase);

        int repetitions = calculateEARepetitions(level, progression);
        int effortDuration = calculateEAEffortDuration(sport, level);
        int recoveryDuration = calculateEARecoveryDuration(effortDuration);

        int warmupDuration = (int)(totalDuration * 0.2);
        int mainWorkDuration = (effortDuration + recoveryDuration) * repetitions;
        int cooldownDuration = Math.max(300, totalDuration - warmupDuration - mainWorkDuration);

        return List.of(
                createWorkoutPlan(1, 1, WorkoutType.EA, List.of(
                        createSegment(1, warmupDuration, IntensityZone.ENDURANCE)
                )),
                createWorkoutPlan(2, repetitions, WorkoutType.EA, List.of(
                        createSegment(1, effortDuration, IntensityZone.TEMPO),
                        createSegment(2, recoveryDuration, IntensityZone.ENDURANCE)
                )),
                createWorkoutPlan(3, 1, WorkoutType.EA, List.of(
                        createSegment(1, cooldownDuration, IntensityZone.RECOVERY)
                ))
        );
    }

    /**
     * Generates sport-specific technical training sessions.
     * Swimming receives specialized technical protocols with multiple drill segments,
     * while other sports get general technical work focused on form and efficiency.
     *
     * @param sport the sport being trained
     * @param level the athlete's fitness level (1-100)
     * @param progression the progression through the training plan (0.0-1.0)
     * @param phase the current training phase
     * @return a structured technical workout plan
     */
    private List<WorkoutPlan> generateTechnic(Sport sport, int level, double progression, TrainingPlanPhase phase) {

        int totalDuration = calculateTotalDuration(sport, WorkoutType.TECHNIC, level, progression, phase);

        if (sport == Sport.SWIMMING) {
            return generateSwimmingTechnic(totalDuration, level);
        } else {
            return generateGeneralTechnic(totalDuration);
        }
    }

    /**
     * Generates active recovery training sessions.
     * These low-intensity sessions promote recovery while maintaining movement
     * and blood flow for enhanced recovery processes.
     *
     * @param sport the sport being trained
     * @param level the athlete's fitness level (1-100)
     * @param progression the progression through the training plan (0.0-1.0)
     * @param phase the current training phase
     * @return a structured recovery workout plan
     */
    private List<WorkoutPlan> generateRecuperationActive(Sport sport, int level, double progression, TrainingPlanPhase phase) {

        int totalDuration = calculateTotalDuration(sport, WorkoutType.RA, level, progression, phase);

        return List.of(
                createWorkoutPlan(1, 1, WorkoutType.RA, List.of(
                        createSegment(1, totalDuration, IntensityZone.RECOVERY)
                ))
        );
    }

    /**
     * Calculates the total workout duration by applying multiple adjustment factors
     * to the base duration. Factors include fitness level, training phase effects,
     * and progression through the training plan.
     *
     * @param sport the sport being trained
     * @param type the workout type
     * @param level the athlete's fitness level (1-100)
     * @param progression the progression through the plan (0.0-1.0)
     * @param phase the current training phase
     * @return the calculated total duration in seconds
     */
    private int calculateTotalDuration(Sport sport, WorkoutType type, int level, double progression, TrainingPlanPhase phase) {

        int baseDuration = BASE_DURATIONS.get(sport).get(type);

        double levelCoeff = calculateLevelCoefficient(level);
        double phaseCoeff = calculatePhaseCoefficient(phase, type);
        double progressionCoeff = calculateProgressionCoefficient(progression);

        return (int) (baseDuration * levelCoeff * phaseCoeff * progressionCoeff);
    }

    /**
     * Calculates adaptive interval training parameters based on training phase,
     * fitness level, and sport requirements. Parameters progressively intensify
     * through the training phases while adapting to the athlete's capabilities.
     *
     * @param phase the current training phase
     * @param level the athlete's fitness level (1-100)
     * @param sport the sport being trained
     * @param progression the progression through the plan (0.0-1.0)
     * @return optimized interval parameters
     */
    private IntervalParams calculateIntervalParams(TrainingPlanPhase phase, int level, Sport sport, double progression) {

        IntervalParams params = new IntervalParams();

        switch (phase) {
            case BASE -> {
                params.repetitions = Math.max(4, (int)(level / 20.0 * (1 + progression * 0.3)));
                params.effortDuration = sport == Sport.SWIMMING ? 240 : 300;
                params.recoveryDuration = (int)(params.effortDuration * 0.6);
                params.effortZone = IntensityZone.TEMPO;
            }
            case SPECIFIC -> {
                params.repetitions = Math.max(6, (int)(level / 15.0 * (1 + progression * 0.2)));
                params.effortDuration = sport == Sport.SWIMMING ? 180 : 240;
                params.recoveryDuration = (int)(params.effortDuration * 0.5);
                params.effortZone = IntensityZone.THRESHOLD;
            }
            case SHARPENING -> {
                params.repetitions = Math.max(8, (int)(level / 10.0 * (1 + progression * 0.1)));
                params.effortDuration = sport == Sport.SWIMMING ? 90 : 120;
                params.recoveryDuration = params.effortDuration;
                params.effortZone = IntensityZone.VO2_MAX;
            }
        }

        return params;
    }

    /**
     * Calculates adaptive lactate threshold training parameters.
     * These parameters optimize the work-to-rest ratio and effort duration
     * based on training phase and athlete development.
     *
     * @param phase the current training phase
     * @param level the athlete's fitness level (1-100)
     * @param sport the sport being trained
     * @param progression the progression through the plan (0.0-1.0)
     * @return optimized lactate threshold parameters
     */
    private LactateParams calculateLactateParams(TrainingPlanPhase phase, int level, Sport sport, double progression) {

        LactateParams params = new LactateParams();

        switch (phase) {
            case BASE -> {
                params.repetitions = Math.max(2, level / 30);
                params.effortDuration = sport == Sport.SWIMMING ? 600 : 720;
                params.recoveryDuration = (int)(params.effortDuration * 0.4);
            }
            case SPECIFIC -> {
                params.repetitions = Math.max(3, (int)(level / 25.0 * (1 + progression * 0.2)));
                params.effortDuration = sport == Sport.SWIMMING ? 480 : 600;
                params.recoveryDuration = (int)(params.effortDuration * 0.35);
            }
            case SHARPENING -> {
                params.repetitions = Math.max(4, (int)(level / 20.0 * (1 + progression * 0.1)));
                params.effortDuration = sport == Sport.SWIMMING ? 360 : 480;
                params.recoveryDuration = (int)(params.effortDuration * 0.3);
            }
        }

        return params;
    }

    /**
     * Creates specialized swimming technical training with multiple drill segments.
     * Higher fitness levels receive more varied drill segments to maintain
     * engagement and provide comprehensive technical development.
     *
     * @param totalDuration the total session duration
     * @param level the athlete's fitness level
     * @return a swimming-specific technical workout plan
     */
    private List<WorkoutPlan> generateSwimmingTechnic(int totalDuration, int level) {

        int drillSegments = Math.max(3, level / 25);
        int segmentDuration = (int)(totalDuration * 0.7 / drillSegments);

        List<WorkoutPlanDetails> mainDetails = new ArrayList<>();
        for (int i = 0; i < drillSegments; i++) {
            mainDetails.add(createSegment(i + 1, segmentDuration, IntensityZone.RECOVERY));
        }

        return List.of(
                createWorkoutPlan(1, 1, WorkoutType.TECHNIC, List.of(
                        createSegment(1, (int)(totalDuration * 0.15), IntensityZone.RECOVERY)
                )),
                createWorkoutPlan(2, 1, WorkoutType.TECHNIC, mainDetails),
                createWorkoutPlan(3, 1, WorkoutType.TECHNIC, List.of(
                        createSegment(1, (int)(totalDuration * 0.15), IntensityZone.RECOVERY)
                ))
        );
    }

    /**
     * Creates general technical training structure for running and cycling.
     * Follows a simple warm-up, main technical work, and cool-down pattern.
     *
     * @param totalDuration the total session duration
     * @return a general technical workout plan
     */
    private List<WorkoutPlan> generateGeneralTechnic(int totalDuration) {

        return List.of(
                createWorkoutPlan(1, 1, WorkoutType.TECHNIC, List.of(
                        createSegment(1, (int)(totalDuration * 0.15), IntensityZone.RECOVERY)
                )),
                createWorkoutPlan(2, 1, WorkoutType.TECHNIC, List.of(
                        createSegment(1, (int)(totalDuration * 0.7), IntensityZone.RECOVERY)
                )),
                createWorkoutPlan(3, 1, WorkoutType.TECHNIC, List.of(
                        createSegment(1, (int)(totalDuration * 0.15), IntensityZone.RECOVERY)
                ))
        );
    }

    /**
     * Calculates the number of repetitions for active endurance training
     * based on fitness level and progression through the training plan.
     *
     * @param level the athlete's fitness level (1-100)
     * @param progression the progression through the plan (0.0-1.0)
     * @return the number of repetitions
     */
    private int calculateEARepetitions(int level, double progression) {
        return Math.max(3, (int)(level / 20.0 * (1 + progression * 0.2)));
    }

    /**
     * Calculates the effort duration for active endurance training
     * with sport-specific base durations and fitness level adjustments.
     *
     * @param sport the sport being trained
     * @param level the athlete's fitness level (1-100)
     * @return the effort duration in seconds
     */
    private int calculateEAEffortDuration(Sport sport, int level) {

        int baseDuration = sport == Sport.SWIMMING ? 360 : 480;
        return (int)(baseDuration * (0.8 + level / 500.0));
    }

    /**
     * Calculates recovery duration for active endurance training.
     * Uses a 25% work-to-rest ratio with a minimum recovery period.
     *
     * @param effortDuration the duration of the effort segment
     * @return the recovery duration in seconds
     */
    private int calculateEARecoveryDuration(int effortDuration) {
        return Math.max(60, effortDuration / 4);
    }

    /**
     * Factory method for creating workout plan objects with consistent structure.
     *
     * @param blocId the block identifier
     * @param repetitions the number of repetitions for this block
     * @param type the workout type
     * @param details the list of workout plan details
     * @return a configured workout plan
     */
    private WorkoutPlan createWorkoutPlan(int blocId, int repetitions, WorkoutType type, List<WorkoutPlanDetails> details) {

        WorkoutPlan plan = new WorkoutPlan();
        plan.setBlocId(blocId);
        plan.setRepetitionCount(repetitions);
        plan.setWorkoutType(type);
        plan.setDetails(details);
        return plan;
    }

    /**
     * Factory method for creating workout plan detail segments with safety constraints.
     *
     * @param id the segment identifier
     * @param duration the duration in seconds
     * @param zone the target intensity zone
     * @return a configured workout plan detail
     */
    private WorkoutPlanDetails createSegment(int id, int duration, IntensityZone zone) {

        WorkoutPlanDetails detail = new WorkoutPlanDetails();
        detail.setBlocDetailId(id);
        detail.setDurationSec(Math.max(30, duration));
        detail.setIntensityZone(zone);
        return detail;
    }

    /**
     * Calculates the fitness level coefficient for duration adjustments.
     * Provides a range from 0.3 for beginners to 2.0 for elite athletes.
     *
     * @param level the athlete's fitness level (1-100)
     * @return the level coefficient (0.3-2.0)
     */
    private double calculateLevelCoefficient(int level) {
        return 0.3 + (level - 1) * (2.0 - 0.3) / 99.0;
    }

    /**
     * Calculates the training phase coefficient for duration adjustments.
     * Base phase emphasizes longer endurance work, while sharpening phase
     * reduces endurance volume to focus on intensity.
     *
     * @param phase the current training phase
     * @param type the workout type
     * @return the phase coefficient
     */
    private double calculatePhaseCoefficient(TrainingPlanPhase phase, WorkoutType type) {

        return switch (phase) {
            case BASE -> type == WorkoutType.EF ? 1.2 : 0.8;
            case SPECIFIC -> 1.0;
            case SHARPENING -> type == WorkoutType.EF ? 0.7 : 0.9;
        };
    }

    /**
     * Calculates the progression coefficient for gradual workout development.
     * Provides a range from 0.8 early in the plan to 1.2 later in the plan.
     *
     * @param progression the progression through the plan (0.0-1.0)
     * @return the progression coefficient (0.8-1.2)
     */
    private double calculateProgressionCoefficient(double progression) {
        return 0.8 + progression * 0.4;
    }

    /**
     * Internal class for storing interval training parameters.
     * Encapsulates all variables needed for adaptive interval generation.
     */
    private static class IntervalParams {
        int repetitions;
        int effortDuration;
        int recoveryDuration;
        IntensityZone effortZone;
    }

    /**
     * Internal class for storing lactate threshold training parameters.
     * Encapsulates variables needed for threshold work generation.
     */
    private static class LactateParams {
        int repetitions;
        int effortDuration;
        int recoveryDuration;
    }
}