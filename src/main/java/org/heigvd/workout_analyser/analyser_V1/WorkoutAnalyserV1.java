package org.heigvd.workout_analyser.analyser_V1;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.dto.workout_dto.WorkoutFullDto;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutStatus;
import org.heigvd.entity.workout.data_point.BPMDataPoint;
import org.heigvd.entity.workout.details.WorkoutPlan;
import org.heigvd.entity.workout.details.WorkoutPlanDetails;
import org.heigvd.service.AIService;
import org.heigvd.service.AccountService;
import org.heigvd.service.FitnessLevelService;
import org.heigvd.service.WorkoutService;
import org.heigvd.workout_analyser.interfaces.WorkoutAnalyser;

import java.util.List;

/**
 * Version 1 implementation of the workout analyzer.
 * This analyzer provides comprehensive workout evaluation including:
 *
 * - Multi-dimensional performance grading (0-10 scale)
 * - Duration compliance analysis
 * - Heart rate zone adherence evaluation
 * - Consistency and intensity assessment
 * - Data completeness verification
 * - AI-powered feedback generation
 * - Fitness level progression tracking
 *
 * The analyzer only processes completed workouts and uses a weighted
 * scoring system to provide objective performance feedback.
 *
 * @version 1.0
 */
@ApplicationScoped
public class WorkoutAnalyserV1 implements WorkoutAnalyser {

    @Inject
    AccountService accountService;

    @Inject
    AIService aiService;

    @Inject
    WorkoutService workoutService;

    @Inject
    FitnessLevelService fitnessLevelService;

    /**
     * Analyzes a completed workout and generates comprehensive performance metrics.
     * This method performs multi-dimensional analysis including grading, AI feedback
     * generation, and fitness level updates based on performance.
     *
     * Only completed workouts are analyzed - planned or in-progress workouts
     * are returned unchanged.
     *
     * @param workout the workout to analyze
     * @return the workout with updated analysis metrics and AI feedback
     */
    @Override
    public Workout analyse(Workout workout) {

        if (workout.getStatus() != WorkoutStatus.COMPLETED) {
            return workout;
        }

        double grade = gradeWorkout(workout);
        workout.setGrade(grade);
        String aiFeedback;

        if(workout.getWorkoutType() == null) {
            aiFeedback = "Workout type is undefined, unable to provide analysis. This could be due to the workout being imported without Training Plan.";
        } else {
            WorkoutFullDto workoutDto = workoutService.toWorkoutFullDto(workout, workout.getAccount().getFCMax());
            aiFeedback = aiService.analyzeSportActivity(workoutDto.toString());
        }

        workout.setAiAnalysis(aiFeedback);
        fitnessLevelService.updateFitnessLevel(workout.getAccount(), workout, grade);

        return workout;
    }

    /**
     * Evaluates workout quality using a comprehensive multi-dimensional scoring system.
     * The grading uses a weighted approach considering five key performance areas:
     *
     * - Duration compliance (25%): How well actual duration matched planned duration
     * - Heart rate compliance (35%): Adherence to planned heart rate zones
     * - Heart rate consistency (20%): Stability and reliability of heart rate data
     * - Intensity appropriateness (10%): Whether intensity matched workout type
     * - Data completeness (10%): Quality and completeness of recorded metrics
     *
     * @param workout the completed workout to evaluate
     * @return a grade between 0.0 and 10.0, rounded to one decimal place
     */
    private double gradeWorkout(Workout workout) {

        if (workout.getStatus() != WorkoutStatus.COMPLETED) {
            return 0.0;
        }

        double totalScore = 0.0;

        double durationScore = evaluateDuration(workout);
        totalScore += durationScore * 0.25;

        double heartRateScore = evaluateHeartRateCompliance(workout);
        totalScore += heartRateScore * 0.35;

        double consistencyScore = evaluateHeartRateConsistency(workout);
        totalScore += consistencyScore * 0.20;

        double intensityScore = evaluateIntensity(workout);
        totalScore += intensityScore * 0.10;

        double dataCompletenessScore = evaluateDataCompleteness(workout);
        totalScore += dataCompletenessScore * 0.10;

        return Math.max(0.0, Math.min(10.0, Math.round(totalScore * 10.0) / 10.0));
    }

    /**
     * Evaluates how well the actual workout duration matched the planned duration.
     * This assessment considers the workout plan's expected duration and compares
     * it against the actual recorded duration using tolerance bands.
     *
     * Scoring bands:
     * - 10.0: Within ±10% of planned duration (optimal)
     * - 8.0: Within ±20% of planned duration (good)
     * - 6.0: Within ±30% of planned duration (acceptable)
     * - 4.0: Within ±50% of planned duration (poor)
     * - 2.0: Beyond ±50% of planned duration (very poor)
     *
     * @param workout the workout to evaluate
     * @return duration compliance score (0.0-10.0)
     */
    private double evaluateDuration(Workout workout) {

        if (workout.getPlans().isEmpty()) {
            return 7.0;
        }

        int plannedDuration = calculatePlannedDuration(workout.getPlans());
        int actualDuration = workout.getDurationSec();

        if (plannedDuration == 0) {
            return 7.0;
        }

        double ratio = (double) actualDuration / plannedDuration;

        if (ratio >= 0.9 && ratio <= 1.1) {
            return 10.0;
        } else if (ratio >= 0.8 && ratio <= 1.2) {
            return 8.0;
        } else if (ratio >= 0.7 && ratio <= 1.3) {
            return 6.0;
        } else if (ratio >= 0.5 && ratio <= 1.5) {
            return 4.0;
        } else {
            return 2.0;
        }
    }

    /**
     * Evaluates adherence to planned heart rate zones throughout the workout.
     * This method analyzes how well the athlete maintained target heart rate
     * zones as specified in the workout plan segments.
     *
     * The evaluation considers each planned segment and compares actual heart rate
     * data against target zone ranges, accounting for individual maximum heart rate.
     *
     * @param workout the workout to evaluate
     * @return heart rate compliance score (0.0-10.0)
     */
    private double evaluateHeartRateCompliance(Workout workout) {

        if (workout.getPlans().isEmpty() || workout.getActualBPMDataPoints().isEmpty()) {
            return 7.0;
        }

        int fcMax = workout.getAccount().getFCMax();
        List<BPMDataPoint> bpmData = workout.getActualBPMDataPoints();

        double totalComplianceScore = 0.0;
        int segmentCount = 0;

        for (WorkoutPlan plan : workout.getPlans()) {
            for (WorkoutPlanDetails detail : plan.getDetails()) {
                double segmentScore = evaluateSegmentCompliance(detail, bpmData, fcMax, plan.getRepetitionCount());
                totalComplianceScore += segmentScore;
                segmentCount++;
            }
        }

        return segmentCount > 0 ? totalComplianceScore / segmentCount : 7.0;
    }

    /**
     * Evaluates heart rate compliance for a specific workout segment.
     * This method determines how well the athlete maintained the target heart rate
     * zone during a particular segment of the workout plan.
     *
     * The evaluation uses tolerance bands around the target zone to account for
     * natural variability in heart rate response and measurement accuracy.
     *
     * @param detail the workout plan segment details
     * @param bpmData the heart rate data points
     * @param fcMax the athlete's maximum heart rate
     * @param repetitions the number of repetitions for this segment
     * @return segment compliance score (0.0-10.0)
     */
    private double evaluateSegmentCompliance(WorkoutPlanDetails detail, List<BPMDataPoint> bpmData,
                                             int fcMax, int repetitions) {

        int targetMin = (int) (fcMax * detail.getIntensityZone().getMinHr());
        int targetMax = (int) (fcMax * detail.getIntensityZone().getMaxHr());

        double avgBpm = bpmData.stream()
                .mapToDouble(BPMDataPoint::getBpm)
                .average()
                .orElse(0.0);

        if (avgBpm == 0.0) {
            return 5.0;
        }

        if (avgBpm >= targetMin && avgBpm <= targetMax) {
            return 10.0;
        }

        double tolerance = (targetMax - targetMin) * 0.1;
        if (avgBpm >= (targetMin - tolerance) && avgBpm <= (targetMax + tolerance)) {
            return 8.5;
        }

        double largeTolerance = (targetMax - targetMin) * 0.2;
        if (avgBpm >= (targetMin - largeTolerance) && avgBpm <= (targetMax + largeTolerance)) {
            return 7.0;
        }

        return 4.0;
    }

    /**
     * Evaluates the consistency of heart rate data throughout the workout.
     * This method uses statistical analysis to assess heart rate stability,
     * which indicates workout quality and data reliability.
     *
     * The evaluation uses coefficient of variation (CV) to measure relative
     * variability, with lower CV indicating more consistent performance.
     *
     * Consistency bands:
     * - 10.0: CV ≤ 5% (very consistent)
     * - 8.0: CV ≤ 10% (good consistency)
     * - 6.0: CV ≤ 15% (average consistency)
     * - 4.0: CV ≤ 25% (inconsistent)
     * - 2.0: CV > 25% (very inconsistent)
     *
     * @param workout the workout to evaluate
     * @return heart rate consistency score (0.0-10.0)
     */
    private double evaluateHeartRateConsistency(Workout workout) {

        List<BPMDataPoint> bpmData = workout.getActualBPMDataPoints();

        if (bpmData.size() < 2) {
            return 7.0;
        }

        double mean = bpmData.stream().mapToDouble(BPMDataPoint::getBpm).average().orElse(0.0);
        double variance = bpmData.stream()
                .mapToDouble(point -> Math.pow(point.getBpm() - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);
        double coefficientOfVariation = mean > 0 ? (stdDev / mean) * 100 : 100;

        if (coefficientOfVariation <= 5) {
            return 10.0;
        } else if (coefficientOfVariation <= 10) {
            return 8.0;
        } else if (coefficientOfVariation <= 15) {
            return 6.0;
        } else if (coefficientOfVariation <= 25) {
            return 4.0;
        } else {
            return 2.0;
        }
    }

    /**
     * Evaluates whether the workout intensity was appropriate for the workout type.
     * This method compares average heart rate against expected ranges for different
     * workout types, ensuring that easy workouts remained easy and hard workouts
     * achieved sufficient intensity.
     *
     * Each workout type has specific target heart rate ranges based on established
     * training zone principles. The evaluation considers these targets and applies
     * appropriate scoring based on adherence.
     *
     * @param workout the workout to evaluate
     * @return intensity appropriateness score (0.0-10.0)
     */
    private double evaluateIntensity(Workout workout) {

        if (workout.getAvgHeartRate() == 0 || workout.getWorkoutType() == null) {
            return 7.0;
        }

        int fcMax = workout.getAccount().getFCMax();
        double intensityPercent = (double) workout.getAvgHeartRate() / fcMax;

        return switch (workout.getWorkoutType()) {
            case EF -> intensityPercent <= 0.7 ? 10.0 :
                    Math.max(2.0, 10.0 - (intensityPercent - 0.7) * 20);
            case EA -> (intensityPercent >= 0.7 && intensityPercent <= 0.8) ? 10.0 :
                    Math.max(2.0, 10.0 - Math.abs(intensityPercent - 0.75) * 40);
            case LACTATE -> (intensityPercent >= 0.8 && intensityPercent <= 0.9) ? 10.0 :
                    Math.max(2.0, 10.0 - Math.abs(intensityPercent - 0.85) * 30);
            case INTERVAL -> (intensityPercent >= 0.85 && intensityPercent <= 0.95) ? 10.0 :
                    Math.max(2.0, 10.0 - Math.abs(intensityPercent - 0.9) * 25);
            case RA -> intensityPercent <= 0.6 ? 10.0 :
                    Math.max(2.0, 10.0 - (intensityPercent - 0.6) * 25);
            case TECHNIC -> intensityPercent <= 0.65 ? 10.0 :
                    Math.max(2.0, 10.0 - (intensityPercent - 0.65) * 22);
            default -> 5.0;
        };
    }

    /**
     * Evaluates the completeness and quality of workout data.
     * This method assesses the presence and coherence of various data points
     * that contribute to a complete workout record.
     *
     * The evaluation considers five key data completeness factors:
     * - Presence of heart rate data points
     * - Presence of speed/pace data points
     * - Coherence of heart rate metrics (avg < max)
     * - Presence of distance data
     * - Presence of calorie data
     *
     * Each factor contributes equally to the final completeness score.
     *
     * @param workout the workout to evaluate
     * @return data completeness score (0.0-10.0)
     */
    private double evaluateDataCompleteness(Workout workout) {

        double score = 0.0;
        int maxPoints = 5;

        if (!workout.getActualBPMDataPoints().isEmpty()) {
            score += 1.0;
        }

        if (!workout.getActualSpeedDataPoints().isEmpty()) {
            score += 1.0;
        }

        if (workout.getAvgHeartRate() > 0 && workout.getMaxHeartRate() > workout.getAvgHeartRate()) {
            score += 1.0;
        }

        if (workout.getDistanceMeters() > 0) {
            score += 1.0;
        }

        if (workout.getCaloriesKcal() > 0) {
            score += 1.0;
        }

        return (score / maxPoints) * 10.0;
    }

    /**
     * Calculates the total planned duration from all workout plan segments.
     * This method sums up the duration of all plan details across all workout plans,
     * accounting for repetitions in each plan block.
     *
     * The calculation considers:
     * - All workout plans in the session
     * - All detail segments within each plan
     * - Repetition counts for each plan block
     *
     * @param plans the list of workout plans
     * @return the total planned duration in seconds
     */
    private int calculatePlannedDuration(List<WorkoutPlan> plans) {

        if (plans == null || plans.isEmpty()) {
            return 0;
        }

        return plans.stream()
                .mapToInt(plan -> {
                    int totalDetailsTime = plan.getDetails().stream()
                            .mapToInt(WorkoutPlanDetails::getDurationSec)
                            .sum();
                    return totalDetailsTime * plan.getRepetitionCount();
                })
                .sum();
    }
}