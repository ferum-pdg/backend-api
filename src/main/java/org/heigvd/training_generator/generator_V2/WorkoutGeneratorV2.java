package org.heigvd.training_generator.generator_V2;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.DailyPlan;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.entity.training_plan.WeeklyPlan;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutStatus;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.entity.workout.details.WorkoutPlan;
import org.heigvd.service.TrainingGeneratorService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.service.WorkoutService;
import org.heigvd.training_generator.interfaces.TrainingWorkoutGenerator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced version 2 implementation of the workout generator.
 * This implementation provides intelligent workout generation with:
 *
 * - Smart workout type distribution based on periodization principles
 * - Dynamic duration calculation considering multiple factors
 * - Phase-aware intensity progression
 * - Sport-specific optimization
 * - Detailed workout plan generation
 *
 * @version 2.0
 */
@ApplicationScoped
public class WorkoutGeneratorV2 implements TrainingWorkoutGenerator {

    @Inject
    TrainingPlanService trainingPlanService;

    @Inject
    WorkoutService workoutService;

    @Inject
    TrainingGeneratorService tgs;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return "V2";
    }

    /**
     * Generates intelligent workouts for the current week and optionally the next week.
     * This method creates workouts with smart type distribution, dynamic durations,
     * and detailed workout plans based on periodization principles.
     *
     * @param trainingPlan the overall training plan
     * @param actualDate the current date for workout generation
     * @return a list of generated workouts
     * @throws IllegalArgumentException if no weekly plan is found for the given date
     */
    @Override
    public List<Workout> generate(TrainingPlan trainingPlan, LocalDate actualDate) {

        Account account = trainingPlan.getAccount();

        Integer currentWeekNumber = trainingPlanService.getWeekNumberForDate(trainingPlan, actualDate);
        WeeklyPlan currentWeek = trainingPlanService.getWeeklyPlanForDate(trainingPlan, account.getId(), actualDate);

        if (currentWeek == null) {
            throw new IllegalArgumentException("No weekly plan found for the given date.");
        }

        LocalDate monday = actualDate.minusDays(actualDate.getDayOfWeek().getValue() - 1);
        List<Workout> workouts = generateWorkoutForWeek(trainingPlan, currentWeek, monday, account, currentWeekNumber);

        if (currentWeekNumber != null && currentWeekNumber < trainingPlan.getWeeklyPlans().size()) {
            workouts.addAll(
                    generateWorkoutForWeek(
                            trainingPlan,
                            trainingPlan.getWeeklyPlans().get(currentWeekNumber),
                            monday.plusWeeks(1),
                            account,
                            currentWeekNumber + 1
                    )
            );
        }

        return workouts;
    }

    /**
     * Synchronizes workouts by generating all missing ones from the last generated
     * workout with a WorkoutPlan up to and including the following week.
     *
     * This method ensures continuity in workout generation and prevents gaps
     * in the training schedule.
     *
     * @param trainingPlan the training plan to synchronize
     * @param today the current date for synchronization reference
     * @return a list of newly generated workouts
     * @throws IllegalArgumentException if the training plan is null
     */
    @Override
    public List<Workout> sync(TrainingPlan trainingPlan, LocalDate today) {

        if (trainingPlan == null) {
            throw new IllegalArgumentException("Training plan cannot be null");
        }

        Account account = trainingPlan.getAccount();
        List<Workout> newWorkouts = new ArrayList<>();

        LocalDate lastGeneratedWorkoutDate = workoutService.getLastGeneratedWorkoutDate(account);
        LocalDate endDate = today.plusWeeks(1).with(java.time.DayOfWeek.SUNDAY);

        if (lastGeneratedWorkoutDate != null && !lastGeneratedWorkoutDate.isBefore(endDate)) {
            return newWorkouts;
        }

        int currentWeekNumber = trainingPlanService.getWeekNumberForDate(trainingPlan, today);

        newWorkouts.addAll(
                generateWorkoutForWeek(
                        trainingPlan,
                        trainingPlan.getWeeklyPlans().get(currentWeekNumber),
                        today.minusDays(today.getDayOfWeek().getValue() - 1),
                        account,
                        currentWeekNumber
                )
        );

        if (currentWeekNumber < trainingPlan.getWeeklyPlans().size()) {
            newWorkouts.addAll(
                    generateWorkoutForWeek(
                            trainingPlan,
                            trainingPlan.getWeeklyPlans().get(currentWeekNumber + 1),
                            today.plusWeeks(1).minusDays(today.plusWeeks(1).getDayOfWeek().getValue() - 1),
                            account,
                            currentWeekNumber + 1
                    )
            );
        }

        return newWorkouts;
    }

    /**
     * Generates workouts for a complete week with intelligent type distribution
     * and dynamic duration calculation.
     *
     * This method applies sophisticated periodization principles to determine
     * optimal workout types and durations based on the training phase,
     * user fitness level, and progression through the plan.
     *
     * @param trainingPlan the overall training plan
     * @param plan the weekly plan to generate workouts for
     * @param monday the Monday of the target week
     * @param account the user account
     * @param weekNumber the week number in the training plan
     * @return a list of workouts for the week
     */
    private List<Workout> generateWorkoutForWeek(TrainingPlan trainingPlan, WeeklyPlan plan,
                                                 LocalDate monday, Account account, Integer weekNumber) {

        List<DailyPlan> dailyPlans = plan.getDailyPlans();
        List<Workout> workouts = new ArrayList<>();

        TrainingPlanPhase currentPhase = getCurrentPhase(trainingPlan, weekNumber);
        double progressionPercent = calculateProgressionPercent(trainingPlan, weekNumber);
        int fitnessLevel = account.getLastFitnessLevel().getFitnessLevel();

        Map<Sport, List<WorkoutType>> sportPatterns = generateSmartWorkoutPatterns(
                dailyPlans, currentPhase, fitnessLevel, progressionPercent);

        Map<Sport, Integer> sportCounters = new HashMap<>();

        for (DailyPlan dp : dailyPlans) {
            WorkoutType workoutType = getNextWorkoutType(dp.getSport(), sportPatterns, sportCounters);

            int estimatedDurationMinutes = calculateDynamicWorkoutDuration(
                    dp.getSport(), workoutType, fitnessLevel, currentPhase, progressionPercent);

            OffsetDateTime startTime = monday
                    .plusDays(dp.getDayOfWeek().getValue() - 1)
                    .atTime(18, 0)
                    .atOffset(OffsetDateTime.now().getOffset());

            List<WorkoutPlan> workoutPlans = tgs.generate(
                    dp.getSport(),
                    workoutType,
                    fitnessLevel,
                    progressionPercent,
                    currentPhase
            );

            Workout workout = new Workout(
                    account,
                    dp.getSport(),
                    startTime,
                    startTime.plusMinutes(estimatedDurationMinutes),
                    "Smart Training Generator V2",
                    WorkoutStatus.PLANNED,
                    workoutType,
                    trainingPlan
            );

            workout.setPlans(workoutPlans);
            workouts.add(workout);
        }

        return workouts;
    }

    /**
     * Generates intelligent workout patterns based on sport, phase, and user level.
     * This method automatically balances workout types according to periodization principles,
     * ensuring optimal distribution of training intensities throughout the week.
     *
     * @param dailyPlans the daily plans for the week
     * @param phase the current training phase
     * @param fitnessLevel the user's fitness level (1-100)
     * @param progression the progression through the plan (0.0-1.0)
     * @return a map of workout patterns by sport
     */
    private Map<Sport, List<WorkoutType>> generateSmartWorkoutPatterns(
            List<DailyPlan> dailyPlans, TrainingPlanPhase phase,
            int fitnessLevel, double progression) {

        Map<Sport, Integer> sportCounts = countSportWorkouts(dailyPlans);
        Map<Sport, List<WorkoutType>> patterns = new HashMap<>();

        for (Map.Entry<Sport, Integer> entry : sportCounts.entrySet()) {
            Sport sport = entry.getKey();
            int count = entry.getValue();

            List<WorkoutType> pattern = buildBalancedPattern(sport, count, phase, fitnessLevel, progression);
            patterns.put(sport, pattern);
        }

        return patterns;
    }

    /**
     * Builds a balanced workout pattern for a specific sport considering all training variables.
     * This method takes into account workout volume, training phase, and fitness level
     * to create an optimal distribution of workout types.
     *
     * @param sport the sport for pattern generation
     * @param workoutCount the number of workouts in the week
     * @param phase the training phase (BASE, SPECIFIC, SHARPENING)
     * @param fitnessLevel the user's fitness level (1-100)
     * @param progression the progression through the plan (0.0-1.0)
     * @return an ordered list of workout types for the week
     */
    private List<WorkoutType> buildBalancedPattern(Sport sport, int workoutCount,
                                                   TrainingPlanPhase phase, int fitnessLevel, double progression) {

        List<WorkoutType> pattern = new ArrayList<>();

        switch (workoutCount) {
            case 1 -> pattern.add(WorkoutType.EF);

            case 2 -> {
                pattern.add(WorkoutType.EF);
                pattern.add(getSecondWorkoutType(phase, fitnessLevel));
            }

            case 3 -> {
                pattern.add(WorkoutType.EF);
                pattern.add(getSecondWorkoutType(phase, fitnessLevel));
                pattern.add(getThirdWorkoutType(sport, phase, fitnessLevel));
            }

            default -> pattern = buildHighVolumePattern(sport, workoutCount, phase, fitnessLevel);
        }

        adjustPatternForProgression(pattern, progression, phase);
        optimizePatternForSport(pattern, sport);

        return pattern;
    }

    /**
     * Determines the optimal second workout type based on training phase and fitness level.
     * This follows periodization principles where intensity gradually increases through phases.
     *
     * @param phase the current training phase
     * @param fitnessLevel the user's fitness level (1-100)
     * @return the recommended workout type for the second session
     */
    private WorkoutType getSecondWorkoutType(TrainingPlanPhase phase, int fitnessLevel) {
        return switch (phase) {
            case BASE -> fitnessLevel > 50 ? WorkoutType.EA : WorkoutType.EF;
            case SPECIFIC -> WorkoutType.EA;
            case SHARPENING -> fitnessLevel > 60 ? WorkoutType.INTERVAL : WorkoutType.EA;
        };
    }

    /**
     * Determines the optimal third workout type considering sport specificity.
     * Swimming prioritizes technique for lower fitness levels, while other sports
     * focus on building aerobic capacity.
     *
     * @param sport the sport being trained
     * @param phase the current training phase
     * @param fitnessLevel the user's fitness level (1-100)
     * @return the recommended workout type for the third session
     */
    private WorkoutType getThirdWorkoutType(Sport sport, TrainingPlanPhase phase, int fitnessLevel) {

        if (sport == Sport.SWIMMING && fitnessLevel < 70) {
            return WorkoutType.TECHNIC;
        }

        return switch (phase) {
            case BASE -> WorkoutType.TECHNIC;
            case SPECIFIC -> fitnessLevel > 65 ? WorkoutType.LACTATE : WorkoutType.EA;
            case SHARPENING -> WorkoutType.LACTATE;
        };
    }

    /**
     * Builds workout patterns for high volume training (4+ workouts per week).
     * Uses classical periodization percentages to distribute training types
     * according to established sports science principles.
     *
     * @param sport the sport being trained
     * @param workoutCount the total number of workouts
     * @param phase the current training phase
     * @param fitnessLevel the user's fitness level (1-100)
     * @return a complete workout pattern for high volume training
     */
    private List<WorkoutType> buildHighVolumePattern(Sport sport, int workoutCount,
                                                     TrainingPlanPhase phase, int fitnessLevel) {

        List<WorkoutType> pattern = new ArrayList<>();
        Map<WorkoutType, Double> percentages = getPhasePercentages(phase);

        for (Map.Entry<WorkoutType, Double> entry : percentages.entrySet()) {
            WorkoutType type = entry.getKey();
            double percentage = entry.getValue();
            int count = Math.max(0, (int) Math.round(workoutCount * percentage));

            for (int i = 0; i < count; i++) {
                pattern.add(type);
            }
        }

        while (pattern.size() < workoutCount) {
            pattern.add(WorkoutType.EF);
        }

        return pattern;
    }

    /**
     * Returns the percentage distribution of workout types for each training phase.
     * These percentages are based on established periodization models used
     * in endurance sports training.
     *
     * @param phase the training phase
     * @return a map of workout type percentages
     */
    private Map<WorkoutType, Double> getPhasePercentages(TrainingPlanPhase phase) {
        return switch (phase) {
            case BASE -> Map.of(
                    WorkoutType.EF, 0.55,
                    WorkoutType.EA, 0.20,
                    WorkoutType.TECHNIC, 0.15,
                    WorkoutType.RA, 0.10
            );
            case SPECIFIC -> Map.of(
                    WorkoutType.EF, 0.40,
                    WorkoutType.EA, 0.25,
                    WorkoutType.LACTATE, 0.20,
                    WorkoutType.INTERVAL, 0.10,
                    WorkoutType.RA, 0.05
            );
            case SHARPENING -> Map.of(
                    WorkoutType.EF, 0.30,
                    WorkoutType.INTERVAL, 0.30,
                    WorkoutType.LACTATE, 0.25,
                    WorkoutType.EA, 0.10,
                    WorkoutType.RA, 0.05
            );
        };
    }

    /**
     * Adjusts the workout pattern based on progression through the training plan.
     * As athletes progress, the pattern can be modified to include more intensity
     * or variation appropriate to their development.
     *
     * @param pattern the workout pattern to adjust
     * @param progression the progression through the plan (0.0-1.0)
     * @param phase the current training phase
     */
    private void adjustPatternForProgression(List<WorkoutType> pattern, double progression, TrainingPlanPhase phase) {

        if (phase == TrainingPlanPhase.BASE && progression > 0.7) {
            replaceWorkoutTypes(pattern, WorkoutType.EF, WorkoutType.EA, 1);
        }

        if (phase == TrainingPlanPhase.SPECIFIC && progression > 0.6) {
            replaceWorkoutTypes(pattern, WorkoutType.EA, WorkoutType.LACTATE, 1);
        }
    }

    /**
     * Optimizes the workout pattern based on sport-specific requirements.
     * Each sport has different emphasis areas that are reflected in the
     * workout distribution.
     *
     * @param pattern the workout pattern to optimize
     * @param sport the sport being trained
     */
    private void optimizePatternForSport(List<WorkoutType> pattern, Sport sport) {

        if (sport == Sport.SWIMMING) {
            replaceWorkoutTypes(pattern, WorkoutType.EF, WorkoutType.TECHNIC, 1);
        } else if (sport == Sport.CYCLING) {
            replaceWorkoutTypes(pattern, WorkoutType.RA, WorkoutType.EF, 1);
        }
    }

    /**
     * Replaces specific workout types in the pattern with alternatives.
     * This utility method supports pattern optimization and progression adjustments.
     *
     * @param pattern the workout pattern to modify
     * @param from the workout type to replace
     * @param to the replacement workout type
     * @param maxReplacements the maximum number of replacements to make
     */
    private void replaceWorkoutTypes(List<WorkoutType> pattern, WorkoutType from,
                                     WorkoutType to, int maxReplacements) {

        int replacements = 0;
        for (int i = 0; i < pattern.size() && replacements < maxReplacements; i++) {
            if (pattern.get(i) == from) {
                pattern.set(i, to);
                replacements++;
            }
        }
    }

    /**
     * Retrieves the next workout type for a sport using round-robin distribution.
     * This ensures even distribution of workout types according to the generated pattern.
     *
     * @param sport the sport being scheduled
     * @param sportPatterns the patterns for each sport
     * @param sportCounters the current counters for each sport
     * @return the next workout type to assign
     */
    private WorkoutType getNextWorkoutType(Sport sport, Map<Sport, List<WorkoutType>> sportPatterns,
                                           Map<Sport, Integer> sportCounters) {

        List<WorkoutType> pattern = sportPatterns.get(sport);
        if (pattern == null || pattern.isEmpty()) {
            return WorkoutType.EF;
        }

        int counter = sportCounters.getOrDefault(sport, 0);
        WorkoutType workoutType = pattern.get(counter % pattern.size());
        sportCounters.put(sport, counter + 1);

        return workoutType;
    }

    /**
     * Calculates dynamic workout duration considering all relevant factors.
     * This method integrates fitness level, training phase, progression, and sport type
     * to provide accurate duration estimates that adapt to the athlete's development.
     *
     * @param sport the sport being trained
     * @param workoutType the type of workout
     * @param fitnessLevel the user's fitness level (1-100)
     * @param phase the current training phase
     * @param progression the progression through the plan (0.0-1.0)
     * @return the calculated workout duration in minutes
     */
    private int calculateDynamicWorkoutDuration(Sport sport, WorkoutType workoutType,
                                                int fitnessLevel, TrainingPlanPhase phase, double progression) {

        int baseDuration = getBaseDuration(sport, workoutType);

        double levelFactor = 0.4 + (fitnessLevel - 1) * (1.8 - 0.4) / 99.0;
        double phaseFactor = calculatePhaseFactor(phase, workoutType);
        double progressionFactor = 0.85 + (progression * 0.30);

        int finalDuration = (int) (baseDuration * levelFactor * phaseFactor * progressionFactor);

        return Math.max(15, Math.min(finalDuration, getMaxDuration(sport)));
    }

    /**
     * Calculates the phase adjustment factor for workout duration.
     * Different phases emphasize different aspects of training, affecting
     * the optimal duration for each workout type.
     *
     * @param phase the current training phase
     * @param workoutType the workout type
     * @return the phase adjustment factor
     */
    private double calculatePhaseFactor(TrainingPlanPhase phase, WorkoutType workoutType) {
        return switch (phase) {
            case BASE -> workoutType == WorkoutType.EF ? 1.15 : 0.85;
            case SPECIFIC -> 1.0;
            case SHARPENING -> workoutType == WorkoutType.EF ? 0.75 : 1.05;
        };
    }

    /**
     * Returns the maximum safe duration for each sport.
     * These limits ensure that workouts remain within reasonable bounds
     * regardless of other calculation factors.
     *
     * @param sport the sport being trained
     * @return the maximum duration in minutes
     */
    private int getMaxDuration(Sport sport) {
        return switch (sport) {
            case SWIMMING -> 75;
            case CYCLING -> 180;
            case RUNNING -> 120;
        };
    }

    /**
     * Determines the current training phase based on the week number.
     *
     * @param trainingPlan the overall training plan
     * @param weekNumber the current week number
     * @return the current training phase
     */
    private TrainingPlanPhase getCurrentPhase(TrainingPlan trainingPlan, Integer weekNumber) {

        if (weekNumber == null) return TrainingPlanPhase.BASE;

        int totalWeeks = trainingPlan.getWeeklyPlans().size();
        int baseWeeks = TrainingPlanPhase.BASE.computeWeeks(totalWeeks);
        int specificWeeks = TrainingPlanPhase.SPECIFIC.computeWeeks(totalWeeks);

        if (weekNumber <= baseWeeks) {
            return TrainingPlanPhase.BASE;
        } else if (weekNumber <= baseWeeks + specificWeeks) {
            return TrainingPlanPhase.SPECIFIC;
        } else {
            return TrainingPlanPhase.SHARPENING;
        }
    }

    /**
     * Calculates the progression percentage through the training plan.
     *
     * @param trainingPlan the overall training plan
     * @param weekNumber the current week number
     * @return the progression percentage (0.0-1.0)
     */
    private double calculateProgressionPercent(TrainingPlan trainingPlan, Integer weekNumber) {

        if (weekNumber == null) return 0.0;
        return Math.min(1.0, (double) weekNumber / trainingPlan.getWeeklyPlans().size());
    }

    /**
     * Counts the number of workouts for each sport in the daily plans.
     *
     * @param dailyPlans the list of daily plans
     * @return a map of sport workout counts
     */
    private Map<Sport, Integer> countSportWorkouts(List<DailyPlan> dailyPlans) {

        Map<Sport, Integer> counts = new HashMap<>();
        for (DailyPlan dp : dailyPlans) {
            counts.merge(dp.getSport(), 1, Integer::sum);
        }
        return counts;
    }

    /**
     * Returns the base duration for a sport and workout type combination.
     * These are the foundation durations before applying adjustment factors.
     *
     * @param sport the sport being trained
     * @param workoutType the type of workout
     * @return the base duration in minutes
     */
    private int getBaseDuration(Sport sport, WorkoutType workoutType) {

        return switch (sport) {
            case RUNNING -> switch (workoutType) {
                case EF -> 45;
                case INTERVAL -> 40;
                case LACTATE -> 50;
                case EA -> 35;
                case TECHNIC -> 30;
                case RA -> 25;
            };
            case CYCLING -> switch (workoutType) {
                case EF -> 90;
                case INTERVAL -> 60;
                case LACTATE -> 75;
                case EA -> 50;
                case TECHNIC -> 45;
                case RA -> 40;
            };
            case SWIMMING -> switch (workoutType) {
                case EF -> 40;
                case INTERVAL -> 35;
                case LACTATE -> 40;
                case EA -> 35;
                case TECHNIC -> 30;
                case RA -> 25;
            };
        };
    }
}