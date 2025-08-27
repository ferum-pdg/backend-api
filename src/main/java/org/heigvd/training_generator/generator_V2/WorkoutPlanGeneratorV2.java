package org.heigvd.training_generator.generator_V2;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.Sport;
import org.heigvd.entity.workout.*;
import org.heigvd.entity.workout.details.*;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class WorkoutPlanGeneratorV2 {

    public List<WorkoutPlan> generateWorkout(
            Sport sport,
            WorkoutType workoutType,
            int fitnessLevel,           // 1-100
            double progressionPercent,  // 0.0-1.0 (% dans le plan)
            TrainingPlanPhase phase
    ) {
        return switch (workoutType) {
            case EF -> generateEnduranceFondamentale(sport, fitnessLevel, progressionPercent, phase);
            case EA -> generateEnduranceActive(sport, fitnessLevel, progressionPercent, phase);
            case LACTATE -> generateLactate(sport, fitnessLevel, progressionPercent, phase);
            case INTERVAL -> generateInterval(sport, fitnessLevel, progressionPercent, phase);
            case TECHNIC -> generateTechnic(sport, fitnessLevel, progressionPercent, phase);
            case RA -> generateRecuperationActive(sport, fitnessLevel, progressionPercent, phase);
            default -> throw new IllegalArgumentException("Type d'entraînement non supporté");
        };
    }

    private List<WorkoutPlan> generateEnduranceFondamentale(Sport sport, int level, double progression, TrainingPlanPhase phase) {
        int baseDuration = getBaseDuration(sport, WorkoutType.EF);

        double levelCoeff = calculateLevelCoefficient(level);
        double phaseCoeff = calculatePhaseCoefficient(phase, WorkoutType.EF);
        double progressionCoeff = calculateProgressionCoefficient(progression);

        int totalDuration = (int) (baseDuration * levelCoeff * phaseCoeff * progressionCoeff);

        List<WorkoutPlan> workoutPlans = new ArrayList<>();

        // 1. Phase Échauffement
        WorkoutPlan warmupPlan = new WorkoutPlan();
        warmupPlan.setBlocId(1);
        warmupPlan.setRepetitionCount(1);
        warmupPlan.setWorkoutType(WorkoutType.EF);

        List<WorkoutPlanDetails> warmupDetails = new ArrayList<>();
        warmupDetails.add(createSegment(1, (int)(totalDuration * 0.1), IntensityZone.RECOVERY));
        warmupPlan.setDetails(warmupDetails);
        workoutPlans.add(warmupPlan);

        // 2. Phase Principale
        WorkoutPlan mainPlan = new WorkoutPlan();
        mainPlan.setBlocId(2);
        mainPlan.setRepetitionCount(1);
        mainPlan.setWorkoutType(WorkoutType.EF);

        List<WorkoutPlanDetails> mainDetails = new ArrayList<>();
        mainDetails.add(createSegment(1, (int)(totalDuration * 0.8), IntensityZone.ENDURANCE));
        mainPlan.setDetails(mainDetails);
        workoutPlans.add(mainPlan);

        // 3. Phase Récupération
        WorkoutPlan cooldownPlan = new WorkoutPlan();
        cooldownPlan.setBlocId(3);
        cooldownPlan.setRepetitionCount(1);
        cooldownPlan.setWorkoutType(WorkoutType.EF);

        List<WorkoutPlanDetails> cooldownDetails = new ArrayList<>();
        cooldownDetails.add(createSegment(1, (int)(totalDuration * 0.1), IntensityZone.RECOVERY));
        cooldownPlan.setDetails(cooldownDetails);
        workoutPlans.add(cooldownPlan);

        return workoutPlans;
    }

    private List<WorkoutPlan> generateInterval(Sport sport, int level, double progression, TrainingPlanPhase phase) {
        int baseDuration = getBaseDuration(sport, WorkoutType.INTERVAL);

        double levelCoeff = calculateLevelCoefficient(level);
        double phaseCoeff = calculatePhaseCoefficient(phase, WorkoutType.INTERVAL);
        double progressionCoeff = calculateProgressionCoefficient(progression);

        int totalDuration = (int) (baseDuration * levelCoeff * phaseCoeff * progressionCoeff);

        IntervalParams params = getIntervalParams(phase, level, sport);

        List<WorkoutPlan> workoutPlans = new ArrayList<>();

        // 1. Phase Échauffement
        WorkoutPlan warmupPlan = new WorkoutPlan();
        warmupPlan.setBlocId(1);
        warmupPlan.setRepetitionCount(1);
        warmupPlan.setWorkoutType(WorkoutType.INTERVAL);

        List<WorkoutPlanDetails> warmupDetails = new ArrayList<>();
        warmupDetails.add(createSegment(1, (int)(totalDuration * 0.25), IntensityZone.ENDURANCE));
        warmupPlan.setDetails(warmupDetails);
        workoutPlans.add(warmupPlan);

        // 2. Phase Principale (Intervalles)
        WorkoutPlan mainPlan = new WorkoutPlan();
        mainPlan.setBlocId(2);
        mainPlan.setRepetitionCount(params.repetitions);
        mainPlan.setWorkoutType(WorkoutType.INTERVAL);

        List<WorkoutPlanDetails> mainDetails = new ArrayList<>();
        // Effort
        mainDetails.add(createSegment(1, params.effortDuration, params.effortZone));
        // Récupération
        mainDetails.add(createSegment(2, params.recoveryDuration, IntensityZone.ENDURANCE));

        mainPlan.setDetails(mainDetails);
        workoutPlans.add(mainPlan);

        // 3. Phase Récupération
        WorkoutPlan cooldownPlan = new WorkoutPlan();
        cooldownPlan.setBlocId(3);
        cooldownPlan.setRepetitionCount(1);
        cooldownPlan.setWorkoutType(WorkoutType.INTERVAL);

        List<WorkoutPlanDetails> cooldownDetails = new ArrayList<>();
        int cooldownDuration = totalDuration - (int)(totalDuration * 0.25) - (params.effortDuration + params.recoveryDuration) * params.repetitions;
        cooldownDetails.add(createSegment(1, Math.max(300, cooldownDuration), IntensityZone.RECOVERY));
        cooldownPlan.setDetails(cooldownDetails);
        workoutPlans.add(cooldownPlan);

        return workoutPlans;
    }

    private List<WorkoutPlan> generateLactate(Sport sport, int level, double progression, TrainingPlanPhase phase) {
        int baseDuration = getBaseDuration(sport, WorkoutType.LACTATE);

        double levelCoeff = calculateLevelCoefficient(level);
        double phaseCoeff = calculatePhaseCoefficient(phase, WorkoutType.LACTATE);
        double progressionCoeff = calculateProgressionCoefficient(progression);

        int totalDuration = (int) (baseDuration * levelCoeff * phaseCoeff * progressionCoeff);

        LactateParams params = getLactateParams(phase, level, sport);

        List<WorkoutPlan> workoutPlans = new ArrayList<>();

        // 1. Phase Échauffement
        WorkoutPlan warmupPlan = new WorkoutPlan();
        warmupPlan.setBlocId(1);
        warmupPlan.setRepetitionCount(1);
        warmupPlan.setWorkoutType(WorkoutType.LACTATE);

        List<WorkoutPlanDetails> warmupDetails = new ArrayList<>();
        warmupDetails.add(createSegment(1, (int)(totalDuration * 0.2), IntensityZone.ENDURANCE));
        warmupPlan.setDetails(warmupDetails);
        workoutPlans.add(warmupPlan);

        // 2. Phase Principale (Seuil)
        WorkoutPlan mainPlan = new WorkoutPlan();
        mainPlan.setBlocId(2);
        mainPlan.setRepetitionCount(params.repetitions);
        mainPlan.setWorkoutType(WorkoutType.LACTATE);

        List<WorkoutPlanDetails> mainDetails = new ArrayList<>();
        // Effort seuil
        mainDetails.add(createSegment(1, params.effortDuration, IntensityZone.THRESHOLD));
        // Récupération
        mainDetails.add(createSegment(2, params.recoveryDuration, IntensityZone.ENDURANCE));

        mainPlan.setDetails(mainDetails);
        workoutPlans.add(mainPlan);

        // 3. Phase Récupération
        WorkoutPlan cooldownPlan = new WorkoutPlan();
        cooldownPlan.setBlocId(3);
        cooldownPlan.setRepetitionCount(1);
        cooldownPlan.setWorkoutType(WorkoutType.LACTATE);

        List<WorkoutPlanDetails> cooldownDetails = new ArrayList<>();
        int cooldownDuration = totalDuration - (int)(totalDuration * 0.2) - (params.effortDuration + params.recoveryDuration) * params.repetitions;
        cooldownDetails.add(createSegment(1, Math.max(300, cooldownDuration), IntensityZone.RECOVERY));
        cooldownPlan.setDetails(cooldownDetails);
        workoutPlans.add(cooldownPlan);

        return workoutPlans;
    }

    private List<WorkoutPlan> generateEnduranceActive(Sport sport, int level, double progression, TrainingPlanPhase phase) {
        int baseDuration = getBaseDuration(sport, WorkoutType.EA);

        double levelCoeff = calculateLevelCoefficient(level);
        double phaseCoeff = calculatePhaseCoefficient(phase, WorkoutType.EA);
        double progressionCoeff = calculateProgressionCoefficient(progression);

        int totalDuration = (int) (baseDuration * levelCoeff * phaseCoeff * progressionCoeff);

        int repetitions = Math.max(3, level / 20);
        int effortDuration = 480; // 8 min
        int recoveryDuration = 120; // 2 min

        List<WorkoutPlan> workoutPlans = new ArrayList<>();

        // 1. Phase Échauffement
        WorkoutPlan warmupPlan = new WorkoutPlan();
        warmupPlan.setBlocId(1);
        warmupPlan.setRepetitionCount(1);
        warmupPlan.setWorkoutType(WorkoutType.EA);

        List<WorkoutPlanDetails> warmupDetails = new ArrayList<>();
        warmupDetails.add(createSegment(1, (int)(totalDuration * 0.2), IntensityZone.ENDURANCE));
        warmupPlan.setDetails(warmupDetails);
        workoutPlans.add(warmupPlan);

        // 2. Phase Principale (Tempo)
        WorkoutPlan mainPlan = new WorkoutPlan();
        mainPlan.setBlocId(2);
        mainPlan.setRepetitionCount(repetitions);
        mainPlan.setWorkoutType(WorkoutType.EA);

        List<WorkoutPlanDetails> mainDetails = new ArrayList<>();
        // Effort tempo
        mainDetails.add(createSegment(1, effortDuration, IntensityZone.TEMPO));
        // Récupération
        mainDetails.add(createSegment(2, recoveryDuration, IntensityZone.ENDURANCE));

        mainPlan.setDetails(mainDetails);
        workoutPlans.add(mainPlan);

        // 3. Phase Récupération
        WorkoutPlan cooldownPlan = new WorkoutPlan();
        cooldownPlan.setBlocId(3);
        cooldownPlan.setRepetitionCount(1);
        cooldownPlan.setWorkoutType(WorkoutType.EA);

        List<WorkoutPlanDetails> cooldownDetails = new ArrayList<>();
        int cooldownDuration = totalDuration - (int)(totalDuration * 0.2) - (effortDuration + recoveryDuration) * repetitions;
        cooldownDetails.add(createSegment(1, Math.max(300, cooldownDuration), IntensityZone.RECOVERY));
        cooldownPlan.setDetails(cooldownDetails);
        workoutPlans.add(cooldownPlan);

        return workoutPlans;
    }

    private List<WorkoutPlan> generateTechnic(Sport sport, int level, double progression, TrainingPlanPhase phase) {
        int baseDuration = getBaseDuration(sport, WorkoutType.TECHNIC);

        double levelCoeff = calculateLevelCoefficient(level);
        double progressionCoeff = calculateProgressionCoefficient(progression);

        int totalDuration = (int) (baseDuration * levelCoeff * progressionCoeff);

        List<WorkoutPlan> workoutPlans = new ArrayList<>();

        // 1. Phase Échauffement
        WorkoutPlan warmupPlan = new WorkoutPlan();
        warmupPlan.setBlocId(1);
        warmupPlan.setRepetitionCount(1);
        warmupPlan.setWorkoutType(WorkoutType.TECHNIC);

        List<WorkoutPlanDetails> warmupDetails = new ArrayList<>();
        warmupDetails.add(createSegment(1, (int)(totalDuration * 0.15), IntensityZone.RECOVERY));
        warmupPlan.setDetails(warmupDetails);
        workoutPlans.add(warmupPlan);

        // 2. Phase Technique
        WorkoutPlan mainPlan = new WorkoutPlan();
        mainPlan.setBlocId(2);
        mainPlan.setRepetitionCount(1);
        mainPlan.setWorkoutType(WorkoutType.TECHNIC);

        List<WorkoutPlanDetails> mainDetails = new ArrayList<>();
        mainDetails.add(createSegment(1, (int)(totalDuration * 0.7), IntensityZone.RECOVERY));
        mainPlan.setDetails(mainDetails);
        workoutPlans.add(mainPlan);

        // 3. Phase Récupération
        WorkoutPlan cooldownPlan = new WorkoutPlan();
        cooldownPlan.setBlocId(3);
        cooldownPlan.setRepetitionCount(1);
        cooldownPlan.setWorkoutType(WorkoutType.TECHNIC);

        List<WorkoutPlanDetails> cooldownDetails = new ArrayList<>();
        cooldownDetails.add(createSegment(1, (int)(totalDuration * 0.15), IntensityZone.RECOVERY));
        cooldownPlan.setDetails(cooldownDetails);
        workoutPlans.add(cooldownPlan);

        return workoutPlans;
    }

    private List<WorkoutPlan> generateRecuperationActive(Sport sport, int level, double progression, TrainingPlanPhase phase) {
        int baseDuration = getBaseDuration(sport, WorkoutType.RA);

        double levelCoeff = calculateLevelCoefficient(level);
        double progressionCoeff = calculateProgressionCoefficient(progression);

        int totalDuration = (int) (baseDuration * levelCoeff * progressionCoeff);

        List<WorkoutPlan> workoutPlans = new ArrayList<>();

        // Une seule phase : récupération active continue
        WorkoutPlan recoveryPlan = new WorkoutPlan();
        recoveryPlan.setBlocId(1);
        recoveryPlan.setRepetitionCount(1);
        recoveryPlan.setWorkoutType(WorkoutType.RA);

        List<WorkoutPlanDetails> recoveryDetails = new ArrayList<>();
        recoveryDetails.add(createSegment(1, totalDuration, IntensityZone.RECOVERY));
        recoveryPlan.setDetails(recoveryDetails);
        workoutPlans.add(recoveryPlan);

        return workoutPlans;
    }

    // Toutes les méthodes utilitaires restent identiques...

    private int getBaseDuration(Sport sport, WorkoutType type) {
        switch (sport) {
            case RUNNING:
                switch (type) {
                    case EF: return 2700; // 45 min
                    case INTERVAL: return 2400; // 40 min
                    case LACTATE: return 3000; // 50 min
                    case EA: return 2100; // 35 min
                    case TECHNIC: return 1500; // 25 min
                    case RA: return 1200; // 20 min
                }
                break;
            case CYCLING:
                switch (type) {
                    case EF: return 5400; // 90 min
                    case INTERVAL: return 3600; // 60 min
                    case LACTATE: return 4200; // 70 min
                    case EA: return 2700; // 45 min
                    case TECHNIC: return 1800; // 30 min
                    case RA: return 2400; // 40 min
                }
                break;
            case SWIMMING:
                switch (type) {
                    case EF: return 2400; // 40 min
                    case INTERVAL: return 1800; // 30 min
                    case LACTATE: return 2100; // 35 min
                    case EA: return 1800; // 30 min
                    case TECHNIC: return 1500; // 25 min
                    case RA: return 1200; // 20 min
                }
                break;
        }
        return 1800; // Défaut 30 min
    }

    private double calculateLevelCoefficient(int level) {
        return 0.3 + (level - 1) * (2.0 - 0.3) / 99.0;
    }

    private double calculatePhaseCoefficient(TrainingPlanPhase phase, WorkoutType type) {
        switch (phase) {
            case BASE:
                return type == WorkoutType.EF ? 1.2 : 0.8;
            case SPECIFIC:
                return 1.0;
            case SHARPENING:
                return type == WorkoutType.EF ? 0.7 : 0.9;
            default:
                return 1.0;
        }
    }

    private double calculateProgressionCoefficient(double progression) {
        return 0.8 + progression * 0.4;
    }

    private IntervalParams getIntervalParams(TrainingPlanPhase phase, int level, Sport sport) {
        IntervalParams params = new IntervalParams();

        switch (phase) {
            case BASE:
                params.repetitions = Math.max(4, level / 20);
                params.effortDuration = 300; // 5 min
                params.recoveryDuration = 180; // 3 min
                params.effortZone = IntensityZone.TEMPO;
                break;
            case SPECIFIC:
                params.repetitions = Math.max(6, level / 15);
                params.effortDuration = 240; // 4 min
                params.recoveryDuration = 120; // 2 min
                params.effortZone = IntensityZone.THRESHOLD;
                break;
            case SHARPENING:
                params.repetitions = Math.max(8, level / 10);
                params.effortDuration = 120; // 2 min
                params.recoveryDuration = 60; // 1 min
                params.effortZone = IntensityZone.VO2_MAX;
                break;
        }

        return params;
    }

    private LactateParams getLactateParams(TrainingPlanPhase phase, int level, Sport sport) {
        LactateParams params = new LactateParams();

        switch (phase) {
            case BASE:
                params.repetitions = Math.max(2, level / 25);
                params.effortDuration = 720; // 12 min
                params.recoveryDuration = 300; // 5 min
                break;
            case SPECIFIC:
                params.repetitions = Math.max(3, level / 20);
                params.effortDuration = 600; // 10 min
                params.recoveryDuration = 240; // 4 min
                break;
            case SHARPENING:
                params.repetitions = Math.max(4, level / 15);
                params.effortDuration = 480; // 8 min
                params.recoveryDuration = 180; // 3 min
                break;
        }

        return params;
    }

    private WorkoutPlanDetails createSegment(int id, int duration, IntensityZone zone) {
        WorkoutPlanDetails detail = new WorkoutPlanDetails();
        detail.setBlocDetailId(id);
        detail.setDurationSec(duration);
        detail.setIntensityZone(zone);
        return detail;
    }

    // Classes internes pour les paramètres
    private static class IntervalParams {
        int repetitions;
        int effortDuration;
        int recoveryDuration;
        IntensityZone effortZone;
    }

    private static class LactateParams {
        int repetitions;
        int effortDuration;
        int recoveryDuration;
    }
}