package org.heigvd.workout_analyser.analyser_V1;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutStatus;
import org.heigvd.entity.workout.data_point.BPMDataPoint;
import org.heigvd.entity.workout.details.WorkoutPlan;
import org.heigvd.entity.workout.details.WorkoutPlanDetails;
import org.heigvd.service.AccountService;
import org.heigvd.service.FitnessLevelService;
import org.heigvd.workout_analyser.interfaces.WorkoutAnalyser;
import java.util.List;

@ApplicationScoped
public class WorkoutAnalyserV1 implements WorkoutAnalyser {

    @Inject
    AccountService accountService;

    @Inject
    FitnessLevelService fitnessLevelService;

    @Override
    public Workout analyse(Workout workout) {
        if (workout.getStatus() != WorkoutStatus.COMPLETED) {
            return workout; // Ne pas analyser les workouts non terminés
        }

        // Calculer la note de l'entraînement
        double grade = gradeWorkout(workout);
        workout.setGrade(grade);

        // Mettre à jour le fitness level si nécessaire
        fitnessLevelService.updateFitnessLevel(workout.getAccount(), workout, grade);

        return workout;
    }

    /**
     * Évalue la qualité de l'entraînement sur une échelle de 0 à 10
     *
     * @param workout L'entraînement à évaluer
     * @return Note entre 0 et 10
     */
    private double gradeWorkout(Workout workout) {
        if (workout.getStatus() != WorkoutStatus.COMPLETED) {
            return 0.0;
        }

        double totalScore = 0.0;

        // 1. Évaluation de la durée (25% du score)
        double durationScore = evaluateDuration(workout);
        totalScore += durationScore * 0.25;

        // 2. Évaluation du respect des zones cardiaques (35% du score)
        double heartRateScore = evaluateHeartRateCompliance(workout);
        totalScore += heartRateScore * 0.35;

        // 3. Évaluation de la consistance cardiaque (20% du score)
        double consistencyScore = evaluateHeartRateConsistency(workout);
        totalScore += consistencyScore * 0.20;

        // 4. Évaluation de l'intensité globale (10% du score)
        double intensityScore = evaluateIntensity(workout);
        totalScore += intensityScore * 0.10;

        // 5. Évaluation de la complétude des données (10% du score)
        double dataCompletenessScore = evaluateDataCompleteness(workout);
        totalScore += dataCompletenessScore * 0.10;

        // Note finale arrondie à une décimale assurée entre 0 et 10
        return Math.max(0.0, Math.min(10.0, Math.round(totalScore * 10.0) / 10.0));
    }

    /**
     * Évalue si la durée de l'entraînement correspond aux attentes
     */
    private double evaluateDuration(Workout workout) {
        if (workout.getPlans().isEmpty()) {
            return 7.0; // Score neutre si pas de plan de référence
        }

        int plannedDuration = calculatePlannedDuration(workout.getPlans());
        int actualDuration = workout.getDurationSec();

        if (plannedDuration == 0) {
            return 7.0; // Score neutre si durée planifiée inconnue
        }

        double ratio = (double) actualDuration / plannedDuration;

        // Score optimal entre 0.9 et 1.1 (±10%)
        if (ratio >= 0.9 && ratio <= 1.1) {
            return 10.0;
        }
        // Score bon entre 0.8 et 1.2 (±20%)
        else if (ratio >= 0.8 && ratio <= 1.2) {
            return 8.0;
        }
        // Score moyen entre 0.7 et 1.3 (±30%)
        else if (ratio >= 0.7 && ratio <= 1.3) {
            return 6.0;
        }
        // Score faible au-delà
        else if (ratio >= 0.5 && ratio <= 1.5) {
            return 4.0;
        } else {
            return 2.0;
        }
    }

    /**
     * Évalue le respect des zones cardiaques planifiées
     */
    private double evaluateHeartRateCompliance(Workout workout) {
        if (workout.getPlans().isEmpty() || workout.getActualBPMDataPoints().isEmpty()) {
            return 7.0; // Score neutre si pas de données
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
     * Évalue la compliance d'un segment spécifique
     */
    private double evaluateSegmentCompliance(WorkoutPlanDetails detail, List<BPMDataPoint> bpmData,
                                             int fcMax, int repetitions) {
        int targetMin = (int) (fcMax * detail.getIntensityZone().getMinHr());
        int targetMax = (int) (fcMax * detail.getIntensityZone().getMaxHr());

        // Calculer la FC moyenne pour ce segment (approximation)
        double avgBpm = bpmData.stream()
                .mapToDouble(BPMDataPoint::getBpm)
                .average()
                .orElse(0.0);

        if (avgBpm == 0.0) {
            return 5.0; // Score neutre si pas de données
        }

        // Évaluer la compliance
        if (avgBpm >= targetMin && avgBpm <= targetMax) {
            return 10.0; // Parfait
        }

        double tolerance = (targetMax - targetMin) * 0.1; // 10% de tolérance

        if (avgBpm >= (targetMin - tolerance) && avgBpm <= (targetMax + tolerance)) {
            return 8.5; // Très bien
        }

        double largeTolerance = (targetMax - targetMin) * 0.2; // 20% de tolérance

        if (avgBpm >= (targetMin - largeTolerance) && avgBpm <= (targetMax + largeTolerance)) {
            return 7.0; // Correct
        }

        return 4.0; // Hors zone
    }

    /**
     * Évalue la consistance de la fréquence cardiaque
     */
    private double evaluateHeartRateConsistency(Workout workout) {
        List<BPMDataPoint> bpmData = workout.getActualBPMDataPoints();

        if (bpmData.size() < 2) {
            return 7.0; // Score neutre si pas assez de données
        }

        // Calculer la variabilité (coefficient de variation)
        double mean = bpmData.stream().mapToDouble(BPMDataPoint::getBpm).average().orElse(0.0);
        double variance = bpmData.stream()
                .mapToDouble(point -> Math.pow(point.getBpm() - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);
        double coefficientOfVariation = mean > 0 ? (stdDev / mean) * 100 : 100;

        // Score basé sur la variabilité
        if (coefficientOfVariation <= 5) {
            return 10.0; // Très consistant
        } else if (coefficientOfVariation <= 10) {
            return 8.0; // Bon
        } else if (coefficientOfVariation <= 15) {
            return 6.0; // Moyen
        } else if (coefficientOfVariation <= 25) {
            return 4.0; // Inconsistant
        } else {
            return 2.0; // Très inconsistant
        }
    }

    /**
     * Évalue l'intensité globale de l'entraînement
     */
    private double evaluateIntensity(Workout workout) {
        if (workout.getAvgHeartRate() == 0) {
            return 7.0; // Score neutre si pas de données
        }

        int fcMax = workout.getAccount().getFCMax();
        double intensityPercent = (double) workout.getAvgHeartRate() / fcMax;

        // Évaluation basée sur le type d'entraînement et l'intensité
        return switch (workout.getWorkoutType()) {
            case EF -> intensityPercent <= 0.7 ? 10.0 : Math.max(2.0, 10.0 - (intensityPercent - 0.7) * 20);
            case EA -> (intensityPercent >= 0.7 && intensityPercent <= 0.8) ? 10.0 :
                    Math.max(2.0, 10.0 - Math.abs(intensityPercent - 0.75) * 40);
            case LACTATE -> (intensityPercent >= 0.8 && intensityPercent <= 0.9) ? 10.0 :
                    Math.max(2.0, 10.0 - Math.abs(intensityPercent - 0.85) * 30);
            case INTERVAL -> (intensityPercent >= 0.85 && intensityPercent <= 0.95) ? 10.0 :
                    Math.max(2.0, 10.0 - Math.abs(intensityPercent - 0.9) * 25);
            case RA -> intensityPercent <= 0.6 ? 10.0 : Math.max(2.0, 10.0 - (intensityPercent - 0.6) * 25);
            case TECHNIC -> intensityPercent <= 0.65 ? 10.0 : Math.max(2.0, 10.0 - (intensityPercent - 0.65) * 22);
        };
    }

    /**
     * Évalue la complétude des données de l'entraînement
     */
    private double evaluateDataCompleteness(Workout workout) {
        double score = 0.0;
        int maxPoints = 5;

        // Présence de données de fréquence cardiaque
        if (!workout.getActualBPMDataPoints().isEmpty()) score += 1.0;

        // Présence de données de vitesse/allure
        if (!workout.getActualSpeedDataPoints().isEmpty()) score += 1.0;

        // Cohérence des métriques de base
        if (workout.getAvgHeartRate() > 0 && workout.getMaxHeartRate() > workout.getAvgHeartRate()) score += 1.0;

        // Présence de distance et calories
        if (workout.getDistanceMeters() > 0) score += 1.0;
        if (workout.getCaloriesKcal() > 0) score += 1.0;

        return (score / maxPoints) * 10.0;
    }

    /**
     * Calcule la durée totale planifiée
     */
    private int calculatePlannedDuration(List<WorkoutPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return 0;
        }

        return plans.stream()
                .mapToInt(plan -> {
                    // Ici on a accès à 'plan'
                    int totalDetailsTime = plan.getDetails().stream()
                            .mapToInt(WorkoutPlanDetails::getDurationSec)
                            .sum();
                    return totalDetailsTime * plan.getRepetitionCount();
                })
                .sum();
    }
}