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

@ApplicationScoped
public class WorkoutPlanGeneratorV2 implements WorkoutPlanGenerator {
    @Override
    public String getVersion() {
        return "V2";
    }

    @Override
    public List<WorkoutPlan> generate(Sport sport, WorkoutType workoutType, int fitnessLevel,
                                      double progressionPercent, TrainingPlanPhase phase) {
        return switch (workoutType) {
            case EF -> generateEnduranceFondamentale(sport, fitnessLevel, progressionPercent, phase);
            case EA -> generateEnduranceActive(sport, fitnessLevel, progressionPercent, phase);
            case LACTATE -> generateLactate(sport, fitnessLevel, progressionPercent, phase);
            case INTERVAL -> generateInterval(sport, fitnessLevel, progressionPercent, phase);
            case TECHNIC -> generateTechnic(sport, fitnessLevel, progressionPercent, phase);
            case RA -> generateRecuperationActive(sport, fitnessLevel, progressionPercent, phase);
            default -> throw new IllegalArgumentException("Type d'entraînement non supporté: " + workoutType);
        };
    }

    /**
     * Génère un plan d'endurance fondamentale avec structure 10-80-10%
     * @param sport Sport concerné
     * @param level Niveau de forme (1-100)
     * @param progression Progression dans le plan (0.0-1.0)
     * @param phase Phase d'entraînement
     * @return Liste des blocs de l'entraînement
     */
    private List<WorkoutPlan> generateEnduranceFondamentale(Sport sport, int level,
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
     * Génère un plan d'intervalles adaptatif selon la phase et le niveau
     * @param sport Sport concerné
     * @param level Niveau de forme (1-100)
     * @param progression Progression dans le plan (0.0-1.0)
     * @param phase Phase d'entraînement
     * @return Liste des blocs de l'entraînement
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
     * Génère un plan de travail au seuil lactique
     * @param sport Sport concerné
     * @param level Niveau de forme (1-100)
     * @param progression Progression dans le plan (0.0-1.0)
     * @param phase Phase d'entraînement
     * @return Liste des blocs de l'entraînement
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
     * Génère un plan d'endurance active (tempo)
     * @param sport Sport concerné
     * @param level Niveau de forme (1-100)
     * @param progression Progression dans le plan (0.0-1.0)
     * @param phase Phase d'entraînement
     * @return Liste des blocs de l'entraînement
     */
    private List<WorkoutPlan> generateEnduranceActive(Sport sport, int level, double progression, TrainingPlanPhase phase) {
        int totalDuration = calculateTotalDuration(sport, WorkoutType.EA, level, progression, phase);

        // Paramètres adaptatifs selon le niveau et la progression
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
     * Génère un plan technique adapté au sport
     * @param sport Sport concerné
     * @param level Niveau de forme (1-100)
     * @param progression Progression dans le plan (0.0-1.0)
     * @param phase Phase d'entraînement
     * @return Liste des blocs de l'entraînement
     */
    private List<WorkoutPlan> generateTechnic(Sport sport, int level, double progression, TrainingPlanPhase phase) {
        int totalDuration = calculateTotalDuration(sport, WorkoutType.TECHNIC, level, progression, phase);

        // Structure adaptée au sport
        if (sport == Sport.SWIMMING) {
            return generateSwimmingTechnic(totalDuration, level);
        } else {
            return generateGeneralTechnic(totalDuration);
        }
    }

    /**
     * Génère un plan de récupération active
     * @param sport Sport concerné
     * @param level Niveau de forme (1-100)
     * @param progression Progression dans le plan (0.0-1.0)
     * @param phase Phase d'entraînement
     * @return Liste des blocs de l'entraînement
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
     * Calcule la durée totale avec tous les facteurs d'ajustement
     * @param sport Sport concerné
     * @param type Type d'entraînement
     * @param level Niveau (1-100)
     * @param progression Progression (0.0-1.0)
     * @param phase Phase d'entraînement
     * @return Durée totale en secondes
     */
    private int calculateTotalDuration(Sport sport, WorkoutType type, int level, double progression, TrainingPlanPhase phase) {
        int baseDuration = BASE_DURATIONS.get(sport).get(type);

        double levelCoeff = calculateLevelCoefficient(level);
        double phaseCoeff = calculatePhaseCoefficient(phase, type);
        double progressionCoeff = calculateProgressionCoefficient(progression);

        return (int) (baseDuration * levelCoeff * phaseCoeff * progressionCoeff);
    }

    /**
     * Calcule les paramètres d'intervalles adaptatifs
     * @param phase Phase d'entraînement
     * @param level Niveau (1-100)
     * @param sport Sport concerné
     * @param progression Progression (0.0-1.0)
     * @return Paramètres d'intervalles optimisés
     */
    private IntervalParams calculateIntervalParams(TrainingPlanPhase phase, int level, Sport sport, double progression) {
        IntervalParams params = new IntervalParams();

        // Adaptation selon la phase
        switch (phase) {
            case BASE -> {
                params.repetitions = Math.max(4, (int)(level / 20.0 * (1 + progression * 0.3)));
                params.effortDuration = sport == Sport.SWIMMING ? 240 : 300; // 4-5 min
                params.recoveryDuration = (int)(params.effortDuration * 0.6); // Récup 60%
                params.effortZone = IntensityZone.TEMPO;
            }
            case SPECIFIC -> {
                params.repetitions = Math.max(6, (int)(level / 15.0 * (1 + progression * 0.2)));
                params.effortDuration = sport == Sport.SWIMMING ? 180 : 240; // 3-4 min
                params.recoveryDuration = (int)(params.effortDuration * 0.5); // Récup 50%
                params.effortZone = IntensityZone.THRESHOLD;
            }
            case SHARPENING -> {
                params.repetitions = Math.max(8, (int)(level / 10.0 * (1 + progression * 0.1)));
                params.effortDuration = sport == Sport.SWIMMING ? 90 : 120; // 1.5-2 min
                params.recoveryDuration = params.effortDuration; // Récup 100%
                params.effortZone = IntensityZone.VO2_MAX;
            }
        }

        return params;
    }

    /**
     * Calcule les paramètres de travail au seuil adaptatifs
     * @param phase Phase d'entraînement
     * @param level Niveau (1-100)
     * @param sport Sport concerné
     * @param progression Progression (0.0-1.0)
     * @return Paramètres de seuil optimisés
     */
    private LactateParams calculateLactateParams(TrainingPlanPhase phase, int level, Sport sport, double progression) {
        LactateParams params = new LactateParams();

        switch (phase) {
            case BASE -> {
                params.repetitions = Math.max(2, level / 30);
                params.effortDuration = sport == Sport.SWIMMING ? 600 : 720; // 10-12 min
                params.recoveryDuration = (int)(params.effortDuration * 0.4); // Récup 40%
            }
            case SPECIFIC -> {
                params.repetitions = Math.max(3, (int)(level / 25.0 * (1 + progression * 0.2)));
                params.effortDuration = sport == Sport.SWIMMING ? 480 : 600; // 8-10 min
                params.recoveryDuration = (int)(params.effortDuration * 0.35); // Récup 35%
            }
            case SHARPENING -> {
                params.repetitions = Math.max(4, (int)(level / 20.0 * (1 + progression * 0.1)));
                params.effortDuration = sport == Sport.SWIMMING ? 360 : 480; // 6-8 min
                params.recoveryDuration = (int)(params.effortDuration * 0.3); // Récup 30%
            }
        }

        return params;
    }

    /**
     * Structure technique spécialisée pour la natation
     * @param totalDuration Durée totale
     * @param level Niveau de forme
     * @return Plan technique natation
     */
    private List<WorkoutPlan> generateSwimmingTechnic(int totalDuration, int level) {
        int drillSegments = Math.max(3, level / 25); // Plus de variété pour niveaux élevés
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
     * Structure technique générale
     * @param totalDuration Durée totale
     * @return Plan technique standard
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

    // Calculs EA adaptatifs
    private int calculateEARepetitions(int level, double progression) {
        return Math.max(3, (int)(level / 20.0 * (1 + progression * 0.2)));
    }

    private int calculateEAEffortDuration(Sport sport, int level) {
        int baseDuration = sport == Sport.SWIMMING ? 360 : 480; // 6-8 min
        return (int)(baseDuration * (0.8 + level / 500.0)); // Ajustement selon niveau
    }

    private int calculateEARecoveryDuration(int effortDuration) {
        return Math.max(60, effortDuration / 4); // Récup 25% minimum 1min
    }

    // Factory methods pour éviter la répétition de code
    private WorkoutPlan createWorkoutPlan(int blocId, int repetitions, WorkoutType type, List<WorkoutPlanDetails> details) {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setBlocId(blocId);
        plan.setRepetitionCount(repetitions);
        plan.setWorkoutType(type);
        plan.setDetails(details);
        return plan;
    }

    private WorkoutPlanDetails createSegment(int id, int duration, IntensityZone zone) {
        WorkoutPlanDetails detail = new WorkoutPlanDetails();
        detail.setBlocDetailId(id);
        detail.setDurationSec(Math.max(30, duration)); // Minimum 30 secondes
        detail.setIntensityZone(zone);
        return detail;
    }

    // Méthodes de calcul des coefficients (inchangées mais documentées)
    private double calculateLevelCoefficient(int level) {
        return 0.3 + (level - 1) * (2.0 - 0.3) / 99.0; // Facteur 0.3 à 2.0
    }

    private double calculatePhaseCoefficient(TrainingPlanPhase phase, WorkoutType type) {
        return switch (phase) {
            case BASE -> type == WorkoutType.EF ? 1.2 : 0.8;
            case SPECIFIC -> 1.0;
            case SHARPENING -> type == WorkoutType.EF ? 0.7 : 0.9;
        };
    }

    private double calculateProgressionCoefficient(double progression) {
        return 0.8 + progression * 0.4; // Facteur 0.8 à 1.2
    }

    // Configuration centralisée des durées de base (en secondes)
    private static final Map<Sport, Map<WorkoutType, Integer>> BASE_DURATIONS = Map.of(
            Sport.RUNNING, Map.of(
                    WorkoutType.EF, 2700,      // 45 min
                    WorkoutType.INTERVAL, 2400, // 40 min
                    WorkoutType.LACTATE, 3000,  // 50 min
                    WorkoutType.EA, 2100,       // 35 min
                    WorkoutType.TECHNIC, 1500,  // 25 min
                    WorkoutType.RA, 1200        // 20 min
            ),
            Sport.CYCLING, Map.of(
                    WorkoutType.EF, 5400,       // 90 min
                    WorkoutType.INTERVAL, 3600, // 60 min
                    WorkoutType.LACTATE, 4200,  // 70 min
                    WorkoutType.EA, 2700,       // 45 min
                    WorkoutType.TECHNIC, 1800,  // 30 min
                    WorkoutType.RA, 2400        // 40 min
            ),
            Sport.SWIMMING, Map.of(
                    WorkoutType.EF, 2400,       // 40 min
                    WorkoutType.INTERVAL, 1800, // 30 min
                    WorkoutType.LACTATE, 2100,  // 35 min
                    WorkoutType.EA, 1800,       // 30 min
                    WorkoutType.TECHNIC, 1500,  // 25 min
                    WorkoutType.RA, 1200        // 20 min
            )
    );

    // Classes internes améliorées
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