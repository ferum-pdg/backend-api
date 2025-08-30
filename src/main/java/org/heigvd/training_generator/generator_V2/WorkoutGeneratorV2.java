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
import org.heigvd.training_generator.interfaces.TrainingWorkoutGenerator;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class WorkoutGeneratorV2 implements TrainingWorkoutGenerator {

    @Inject
    TrainingPlanService trainingPlanService;

    @Inject
    TrainingGeneratorService tgs;

    @Override
    public String getVersion() {
        return "V2";
    }

    @Override
    public List<Workout> generate(TrainingPlan trainingPlan, LocalDate actualDate) {
        Account account = trainingPlan.getAccount();

        Integer currentWeekNumber = trainingPlanService.getWeekNumberForDate(trainingPlan, actualDate);
        WeeklyPlan currentWeek = trainingPlanService.getWeeklyPlanForDate(trainingPlan, account.getId(), actualDate);

        if(currentWeek == null) {
            throw new IllegalArgumentException("No weekly plan found for the given date.");
        }

        LocalDate monday = actualDate.minusDays(actualDate.getDayOfWeek().getValue() - 1);

        List<Workout> workouts = generateWorkoutForWeek(trainingPlan, currentWeek, monday, account, currentWeekNumber);

        // Génération optionnelle de la semaine suivante
        if(currentWeekNumber != null && currentWeekNumber < trainingPlan.getWeeklyPlans().size()) {
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
     * Génère les entraînements pour une semaine complète
     * @param trainingPlan Plan d'entraînement global
     * @param plan Plan hebdomadaire
     * @param monday Lundi de la semaine concernée
     * @param account Compte utilisateur
     * @param weekNumber Numéro de la semaine
     * @return Liste des entraînements de la semaine
     */
    private List<Workout> generateWorkoutForWeek(TrainingPlan trainingPlan, WeeklyPlan plan,
                                                 LocalDate monday, Account account, Integer weekNumber) {
        List<DailyPlan> dailyPlans = plan.getDailyPlans();
        List<Workout> workouts = new ArrayList<>();

        // Calcul des paramètres globaux de la semaine
        TrainingPlanPhase currentPhase = getCurrentPhase(trainingPlan, weekNumber);
        double progressionPercent = calculateProgressionPercent(trainingPlan, weekNumber);
        int fitnessLevel = account.getLastFitnessLevel().getFitnessLevel();

        // Génération intelligente des types d'entraînements par sport
        Map<Sport, List<WorkoutType>> sportPatterns = generateSmartWorkoutPatterns(
                dailyPlans, currentPhase, fitnessLevel, progressionPercent);

        // Compteurs pour distribuer équitablement les types d'entraînements
        Map<Sport, Integer> sportCounters = new HashMap<>();

        for(DailyPlan dp : dailyPlans) {
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
                    workoutType
            );

            workout.setPlans(workoutPlans);
            workouts.add(workout);
        }

        return workouts;
    }

    /**
     * Génère des patterns d'entraînement intelligents basés sur le sport, la phase et le niveau
     * Équilibre automatiquement les types selon les principes de periodisation
     * @param dailyPlans Plans quotidiens de la semaine
     * @param phase Phase actuelle du plan
     * @param fitnessLevel Niveau de forme (1-100)
     * @param progression Avancement dans le plan (0.0-1.0)
     * @return Map des patterns par sport
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
     * Construit un pattern équilibré d'entraînements pour un sport donné
     * Prend en compte le volume, la phase et le niveau pour optimiser la distribution
     * @param sport Sport concerné (natation, vélo, course)
     * @param workoutCount Nombre d'entraînements dans la semaine
     * @param phase Phase d'entraînement (BASE, SPECIFIC, SHARPENING)
     * @param fitnessLevel Niveau de forme (1-100)
     * @param progression Avancement dans le plan (0.0-1.0)
     * @return Liste ordonnée des types d'entraînements
     */
    private List<WorkoutType> buildBalancedPattern(Sport sport, int workoutCount,
                                                   TrainingPlanPhase phase, int fitnessLevel, double progression) {
        List<WorkoutType> pattern = new ArrayList<>();

        // Pattern de base selon le volume
        switch (workoutCount) {
            case 1:
                pattern.add(WorkoutType.EF);
                break;

            case 2:
                pattern.add(WorkoutType.EF);
                pattern.add(getSecondWorkoutType(phase, fitnessLevel));
                break;

            case 3:
                pattern.add(WorkoutType.EF);
                pattern.add(getSecondWorkoutType(phase, fitnessLevel));
                pattern.add(getThirdWorkoutType(sport, phase, fitnessLevel));
                break;

            default:
                // 4+ entraînements : distribution équilibrée
                pattern = buildHighVolumePattern(sport, workoutCount, phase, fitnessLevel);
        }

        // Ajustements dynamiques selon la progression
        adjustPatternForProgression(pattern, progression, phase);

        // Optimisations spécifiques par sport
        optimizePatternForSport(pattern, sport);

        return pattern;
    }

    /**
     * Détermine le type du deuxième entraînement selon la phase et le niveau
     * @param phase Phase d'entraînement
     * @param fitnessLevel Niveau de forme
     * @return Type d'entraînement optimal
     */
    private WorkoutType getSecondWorkoutType(TrainingPlanPhase phase, int fitnessLevel) {
        return switch (phase) {
            case BASE -> fitnessLevel > 50 ? WorkoutType.EA : WorkoutType.EF;
            case SPECIFIC -> WorkoutType.EA;
            case SHARPENING -> fitnessLevel > 60 ? WorkoutType.INTERVAL : WorkoutType.EA;
        };
    }

    /**
     * Détermine le type du troisième entraînement en tenant compte du sport
     * @param sport Sport concerné
     * @param phase Phase d'entraînement
     * @param fitnessLevel Niveau de forme
     * @return Type d'entraînement optimal
     */
    private WorkoutType getThirdWorkoutType(Sport sport, TrainingPlanPhase phase, int fitnessLevel) {
        if (sport == Sport.SWIMMING && fitnessLevel < 70) {
            return WorkoutType.TECHNIC; // Priorité technique pour natation niveau moyen
        }

        return switch (phase) {
            case BASE -> WorkoutType.TECHNIC;
            case SPECIFIC -> fitnessLevel > 65 ? WorkoutType.LACTATE : WorkoutType.EA;
            case SHARPENING -> WorkoutType.LACTATE;
        };
    }

    /**
     * Construit un pattern pour volume élevé (4+ entraînements)
     * Distribution basée sur les pourcentages de periodisation classique
     * @param sport Sport concerné
     * @param workoutCount Nombre total d'entraînements
     * @param phase Phase d'entraînement
     * @param fitnessLevel Niveau de forme
     * @return Pattern complet
     */
    private List<WorkoutType> buildHighVolumePattern(Sport sport, int workoutCount,
                                                     TrainingPlanPhase phase, int fitnessLevel) {
        List<WorkoutType> pattern = new ArrayList<>();

        // Calculs des quotas selon la phase
        Map<WorkoutType, Double> percentages = getPhasePercentages(phase);

        for (Map.Entry<WorkoutType, Double> entry : percentages.entrySet()) {
            WorkoutType type = entry.getKey();
            double percentage = entry.getValue();
            int count = Math.max(0, (int) Math.round(workoutCount * percentage));

            for (int i = 0; i < count; i++) {
                pattern.add(type);
            }
        }

        // Compléter avec EF si nécessaire
        while (pattern.size() < workoutCount) {
            pattern.add(WorkoutType.EF);
        }

        return pattern;
    }

    /**
     * Retourne les pourcentages de distribution par phase d'entraînement
     * @param phase Phase d'entraînement
     * @return Map des pourcentages par type d'entraînement
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
     * Ajuste le pattern selon l'avancement dans le plan
     * Plus on avance, plus on peut intensifier
     * @param pattern Pattern à ajuster
     * @param progression Avancement (0.0 = début, 1.0 = fin)
     * @param phase Phase actuelle
     */
    private void adjustPatternForProgression(List<WorkoutType> pattern, double progression, TrainingPlanPhase phase) {
        // En fin de phase BASE : ajouter plus de variété
        if (phase == TrainingPlanPhase.BASE && progression > 0.7) {
            replaceWorkoutTypes(pattern, WorkoutType.EF, WorkoutType.EA, 1);
        }

        // En phase SPECIFIC avancée : plus d'intensité
        if (phase == TrainingPlanPhase.SPECIFIC && progression > 0.6) {
            replaceWorkoutTypes(pattern, WorkoutType.EA, WorkoutType.LACTATE, 1);
        }
    }

    /**
     * Optimise le pattern selon les spécificités du sport
     * @param pattern Pattern à optimiser
     * @param sport Sport concerné
     */
    private void optimizePatternForSport(List<WorkoutType> pattern, Sport sport) {
        if (sport == Sport.SWIMMING) {
            // Natation : plus de technique, moins de volume long
            replaceWorkoutTypes(pattern, WorkoutType.EF, WorkoutType.TECHNIC, 1);
        } else if (sport == Sport.CYCLING) {
            // Vélo : privilégier l'endurance longue
            replaceWorkoutTypes(pattern, WorkoutType.RA, WorkoutType.EF, 1);
        }
    }

    /**
     * Remplace des types d'entraînements dans le pattern
     * @param pattern Pattern à modifier
     * @param from Type à remplacer
     * @param to Type de remplacement
     * @param maxReplacements Nombre maximum de remplacements
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
     * Récupère le prochain type d'entraînement pour un sport donné
     * Distribue équitablement les types selon le pattern généré
     * @param sport Sport concerné
     * @param sportPatterns Patterns par sport
     * @param sportCounters Compteurs pour chaque sport
     * @return Type d'entraînement à assigner
     */
    private WorkoutType getNextWorkoutType(Sport sport, Map<Sport, List<WorkoutType>> sportPatterns,
                                           Map<Sport, Integer> sportCounters) {
        List<WorkoutType> pattern = sportPatterns.get(sport);
        if (pattern == null || pattern.isEmpty()) {
            return WorkoutType.EF; // Fallback sécurisé
        }

        int counter = sportCounters.getOrDefault(sport, 0);
        WorkoutType workoutType = pattern.get(counter % pattern.size());
        sportCounters.put(sport, counter + 1);

        return workoutType;
    }

    /**
     * Calcule la durée dynamique d'un entraînement selon tous les paramètres
     * Intègre niveau, phase, progression et type pour un calcul précis
     * @param sport Sport concerné
     * @param workoutType Type d'entraînement
     * @param fitnessLevel Niveau de forme (1-100)
     * @param phase Phase d'entraînement
     * @param progression Avancement dans le plan (0.0-1.0)
     * @return Durée en minutes
     */
    private int calculateDynamicWorkoutDuration(Sport sport, WorkoutType workoutType,
                                                int fitnessLevel, TrainingPlanPhase phase, double progression) {
        // Durées de base par sport et type
        int baseDuration = getBaseDuration(sport, workoutType);

        // Facteur niveau : de 0.4 (débutant) à 1.8 (expert)
        double levelFactor = 0.4 + (fitnessLevel - 1) * (1.8 - 0.4) / 99.0;

        // Facteur phase : ajustement selon l'objectif de la phase
        double phaseFactor = calculatePhaseFactor(phase, workoutType);

        // Facteur progression : augmentation graduelle dans le temps
        double progressionFactor = 0.85 + (progression * 0.30); // de 85% à 115%

        int finalDuration = (int) (baseDuration * levelFactor * phaseFactor * progressionFactor);

        // Limites de sécurité
        return Math.max(15, Math.min(finalDuration, getMaxDuration(sport)));
    }

    /**
     * Facteur d'ajustement selon la phase d'entraînement
     * @param phase Phase actuelle
     * @param workoutType Type d'entraînement
     * @return Facteur multiplicateur
     */
    private double calculatePhaseFactor(TrainingPlanPhase phase, WorkoutType workoutType) {
        return switch (phase) {
            case BASE -> workoutType == WorkoutType.EF ? 1.15 : 0.85;
            case SPECIFIC -> 1.0;
            case SHARPENING -> workoutType == WorkoutType.EF ? 0.75 : 1.05;
        };
    }

    /**
     * Durées maximales de sécurité par sport
     * @param sport Sport concerné
     * @return Durée max en minutes
     */
    private int getMaxDuration(Sport sport) {
        return switch (sport) {
            case SWIMMING -> 75; // Max 75min natation
            case CYCLING -> 180; // Max 3h vélo
            case RUNNING -> 120; // Max 2h course
        };
    }

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

    private double calculateProgressionPercent(TrainingPlan trainingPlan, Integer weekNumber) {
        if (weekNumber == null) return 0.0;
        return Math.min(1.0, (double) weekNumber / trainingPlan.getWeeklyPlans().size());
    }

    private Map<Sport, Integer> countSportWorkouts(List<DailyPlan> dailyPlans) {
        Map<Sport, Integer> counts = new HashMap<>();
        for (DailyPlan dp : dailyPlans) {
            counts.merge(dp.getSport(), 1, Integer::sum);
        }
        return counts;
    }

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