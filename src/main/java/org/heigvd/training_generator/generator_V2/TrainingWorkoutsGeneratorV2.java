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
public class TrainingWorkoutsGeneratorV2 implements TrainingWorkoutGenerator {

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

        // Get the monday of the current week
        LocalDate monday = actualDate.minusDays(actualDate.getDayOfWeek().getValue() - 1);

        List<Workout> workouts = generateWorkoutForWeek(trainingPlan, currentWeek, monday, account, currentWeekNumber);

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

    private List<Workout> generateWorkoutForWeek(TrainingPlan trainingPlan, WeeklyPlan plan, LocalDate monday, Account account, Integer weekNumber) {
        List<DailyPlan> dailyPlans = plan.getDailyPlans();
        List<Workout> workouts = new ArrayList<>();

        // Déterminer la phase actuelle
        TrainingPlanPhase currentPhase = getCurrentPhase(trainingPlan, weekNumber);

        // Calculer la progression dans le plan (0.0 à 1.0)
        double progressionPercent = calculateProgressionPercent(trainingPlan, weekNumber);

        // Compter les entraînements par sport dans cette semaine
        Map<Sport, Integer> sportCounts = countSportWorkouts(dailyPlans);

        // Générer le pattern d'entraînements pour chaque sport
        Map<Sport, List<WorkoutType>> sportPatterns = generateWorkoutPatterns(sportCounts, currentPhase);

        for(DailyPlan dp : dailyPlans) {
            // Récupérer le type d'entraînement pour ce sport et ce jour
            WorkoutType workoutType = getNextWorkoutType(dp.getSport(), sportPatterns);

            // Calculer la durée estimée
            int estimatedDurationMinutes = calculateWorkoutDuration(dp.getSport(), workoutType, account.getLastFitnessLevel().getFitnessLevel(), currentPhase);

            // Créer l'heure de début (18:00 par défaut)
            OffsetDateTime startTime = monday
                    .plusDays(dp.getDayOfWeek().getValue() - 1)
                    .atTime(18, 0)
                    .atOffset(OffsetDateTime.now().getOffset());

            // Générer les WorkoutPlans détaillés
            List<WorkoutPlan> workoutPlans = tgs.generate(
                    dp.getSport(),
                    workoutType,
                    account.getLastFitnessLevel().getFitnessLevel(),
                    progressionPercent,
                    currentPhase
            );

            Workout workout = new Workout(
                    account,
                    dp.getSport(),
                    startTime,
                    startTime.plusMinutes(estimatedDurationMinutes),
                    "Training Workout Generator V2",
                    WorkoutStatus.PLANNED,
                    workoutType
            );

            // Assigner les plans détaillés au workout
            workout.setPlans(workoutPlans);

            workouts.add(workout);
        }

        return workouts;
    }

    private TrainingPlanPhase getCurrentPhase(TrainingPlan trainingPlan, Integer weekNumber) {
        if (weekNumber == null) return TrainingPlanPhase.BASE;

        int totalWeeks = trainingPlan.getWeeklyPlans().size();

        // Calculer les semaines pour chaque phase
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

    private Map<Sport, List<WorkoutType>> generateWorkoutPatterns(Map<Sport, Integer> sportCounts, TrainingPlanPhase phase) {
        Map<Sport, List<WorkoutType>> patterns = new HashMap<>();

        for (Map.Entry<Sport, Integer> entry : sportCounts.entrySet()) {
            Sport sport = entry.getKey();
            int count = entry.getValue();

            patterns.put(sport, generatePatternForSport(sport, count, phase));
        }

        return patterns;
    }

    private List<WorkoutType> generatePatternForSport(Sport sport, int workoutCount, TrainingPlanPhase phase) {
        List<WorkoutType> pattern = new ArrayList<>();

        switch (workoutCount) {
            case 1:
                // Un seul entraînement : priorité endurance fondamentale
                pattern.add(WorkoutType.EF);
                break;

            case 2:
                switch (phase) {
                    case BASE:
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.EF);
                        break;
                    case SPECIFIC:
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.EA);
                        break;
                    case SHARPENING:
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.INTERVAL);
                        break;
                }
                break;

            case 3:
                switch (phase) {
                    case BASE:
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.TECHNIC);
                        break;
                    case SPECIFIC:
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.EA);
                        pattern.add(WorkoutType.LACTATE);
                        break;
                    case SHARPENING:
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.INTERVAL);
                        pattern.add(WorkoutType.LACTATE);
                        break;
                }
                break;

            case 4:
                switch (phase) {
                    case BASE:
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.EA);
                        pattern.add(WorkoutType.TECHNIC);
                        break;
                    case SPECIFIC:
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.EA);
                        pattern.add(WorkoutType.LACTATE);
                        pattern.add(WorkoutType.INTERVAL);
                        break;
                    case SHARPENING:
                        pattern.add(WorkoutType.EF);
                        pattern.add(WorkoutType.INTERVAL);
                        pattern.add(WorkoutType.LACTATE);
                        pattern.add(WorkoutType.RA);
                        break;
                }
                break;

            default:
                // 5+ entraînements : distribution équilibrée
                switch (phase) {
                    case BASE:
                        // 60% EF, 20% EA, 10% TECHNIC, 10% RA
                        addWorkoutsToPattern(pattern, WorkoutType.EF, (int)(workoutCount * 0.6));
                        addWorkoutsToPattern(pattern, WorkoutType.EA, (int)(workoutCount * 0.2));
                        addWorkoutsToPattern(pattern, WorkoutType.TECHNIC, Math.max(1, (int)(workoutCount * 0.1)));
                        addWorkoutsToPattern(pattern, WorkoutType.RA, Math.max(1, workoutCount - pattern.size()));
                        break;
                    case SPECIFIC:
                        // 40% EF, 25% EA, 20% LACTATE, 10% INTERVAL, 5% RA
                        addWorkoutsToPattern(pattern, WorkoutType.EF, (int)(workoutCount * 0.4));
                        addWorkoutsToPattern(pattern, WorkoutType.EA, (int)(workoutCount * 0.25));
                        addWorkoutsToPattern(pattern, WorkoutType.LACTATE, (int)(workoutCount * 0.2));
                        addWorkoutsToPattern(pattern, WorkoutType.INTERVAL, Math.max(1, (int)(workoutCount * 0.1)));
                        addWorkoutsToPattern(pattern, WorkoutType.RA, Math.max(1, workoutCount - pattern.size()));
                        break;
                    case SHARPENING:
                        // 30% EF, 30% INTERVAL, 20% LACTATE, 10% EA, 10% RA
                        addWorkoutsToPattern(pattern, WorkoutType.EF, (int)(workoutCount * 0.3));
                        addWorkoutsToPattern(pattern, WorkoutType.INTERVAL, (int)(workoutCount * 0.3));
                        addWorkoutsToPattern(pattern, WorkoutType.LACTATE, (int)(workoutCount * 0.2));
                        addWorkoutsToPattern(pattern, WorkoutType.EA, Math.max(1, (int)(workoutCount * 0.1)));
                        addWorkoutsToPattern(pattern, WorkoutType.RA, Math.max(1, workoutCount - pattern.size()));
                        break;
                }
                break;
        }

        return pattern;
    }

    private void addWorkoutsToPattern(List<WorkoutType> pattern, WorkoutType type, int count) {
        for (int i = 0; i < count; i++) {
            pattern.add(type);
        }
    }

    // Compteurs pour suivre quel type d'entraînement assigner à chaque sport
    private Map<Sport, Integer> sportCounters = new HashMap<>();

    private WorkoutType getNextWorkoutType(Sport sport, Map<Sport, List<WorkoutType>> sportPatterns) {
        List<WorkoutType> pattern = sportPatterns.get(sport);
        if (pattern == null || pattern.isEmpty()) {
            return WorkoutType.EF; // Fallback
        }

        int counter = sportCounters.getOrDefault(sport, 0);
        WorkoutType workoutType = pattern.get(counter % pattern.size());
        sportCounters.put(sport, counter + 1);

        return workoutType;
    }

    private int calculateWorkoutDuration(Sport sport, WorkoutType workoutType, int fitnessLevel, TrainingPlanPhase phase) {
        // Durées de base en minutes (reprendre la logique du générateur)
        int baseDuration = switch (sport) {
            case RUNNING -> switch (workoutType) {
                case EF -> 45;
                case INTERVAL -> 40;
                case LACTATE -> 50;
                case EA -> 35;
                case TECHNIC -> 25;
                case RA -> 20;
            };
            case CYCLING -> switch (workoutType) {
                case EF -> 90;
                case INTERVAL -> 60;
                case LACTATE -> 70;
                case EA -> 45;
                case TECHNIC -> 30;
                case RA -> 40;
            };
            case SWIMMING -> switch (workoutType) {
                case EF -> 40;
                case INTERVAL -> 30;
                case LACTATE -> 35;
                case EA -> 30;
                case TECHNIC -> 25;
                case RA -> 20;
            };
        };

        // Ajustements selon niveau (1-100 -> 0.3-2.0)
        double levelCoeff = 0.3 + (fitnessLevel - 1) * (2.0 - 0.3) / 99.0;

        // Ajustements selon phase
        double phaseCoeff = switch (phase) {
            case BASE -> workoutType == WorkoutType.EF ? 1.2 : 0.8;
            case SPECIFIC -> 1.0;
            case SHARPENING -> workoutType == WorkoutType.EF ? 0.7 : 0.9;
        };

        return (int)(baseDuration * levelCoeff * phaseCoeff);
    }
}