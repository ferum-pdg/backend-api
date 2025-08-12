package org.heigvd.training_engine;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.*;
import org.heigvd.entity.TrainingPlan.DailyPlan;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.Workout.IntensityZone;
import org.heigvd.entity.Workout.PlannedDataPoint;
import org.heigvd.entity.Workout.Workout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class TrainingGeneratorV1 implements TrainingGenerator {

    public static boolean CAN_HAVE_MULTIPLE_WORKOUTS_PER_DAY = false;

    @Override
    public void generateTrainingWorkouts(TrainingPlan trainingPlan) {

        List<Goal> goals = trainingPlan.getGoals();

        int nbOfWorkoutsPerWeek = calculateNbOfWorkoutsPerWeek(goals, trainingPlan.getAccount());

        int nbOfTrainingWeeks = calculateNbOfWeeks(trainingPlan);

        // Start date calcul
        LocalDate startDate = trainingPlan.getEndDate().minusWeeks(nbOfTrainingWeeks);

        CAN_HAVE_MULTIPLE_WORKOUTS_PER_DAY = trainingPlan.getAccount().getLastFitnessLevel().getFitnessLevel() >= 60 && goals.size() > 1;

        if(nbOfWorkoutsPerWeek > trainingPlan.getDaysOfWeek().size() && !CAN_HAVE_MULTIPLE_WORKOUTS_PER_DAY) {
            throw new IllegalArgumentException("Not enough available days to schedule the workouts.");
        }

        LinkedHashMap<Sport, Integer> sportDistribution = calculateSportDistribution(nbOfWorkoutsPerWeek, goals);

        List<DailyPlan> dailyPlans = generateWeeklyDailyPlans(trainingPlan, sportDistribution);

        trainingPlan.setPairWeeklyPlans(dailyPlans);
        trainingPlan.setStartDate(startDate);

        // On ne génère pas les Workout pour l'instant

        List<Workout> workouts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate firstWeekStart = today.isBefore(startDate) ? startDate : today;

        for (DailyPlan dp : dailyPlans) {
            LocalDate workoutDate = firstWeekStart.with(dp.getDayOfWeek());
            workouts.add(generateWorkout(trainingPlan, workoutDate, dp.getSport()));
        }

        trainingPlan.setWorkouts(workouts);
    }

    private Workout generateWorkout(TrainingPlan tp, LocalDate workoutDate, Sport sport) {
        // Transformer LocalDate en OffsetDateTime basé sur la timezone de l’utilisateur
        OffsetDateTime start = workoutDate.atTime(18, 0) // 18h par défaut
                .atZone(OffsetDateTime.now().getOffset()) // Utiliser l'offset actuel
                .toOffsetDateTime();

        // Durée par défaut selon le sport (V1)
        int durationSec;
        switch (sport) {
            case RUNNING -> durationSec = 60 * 45; // 45 min
            case CYCLING -> durationSec = 60 * 90; // 1h30
            case SWIMMING -> durationSec = 60 * 30; // 30 min
            default -> durationSec = 60 * 45;
        }

        OffsetDateTime end = start.plusSeconds(durationSec);

        List<PlannedDataPoint> plannedDataPoints = new ArrayList<>();

        // Règles simples de phases (V1)
        int warmupSec = (int) (durationSec * 0.2);
        int mainSec = (int) (durationSec * 0.7);

        OffsetDateTime phaseStart = start;

        // Phase 1 : Échauffement (Zone 1, ~60% FC max)
        plannedDataPoints.add(
                new PlannedDataPoint(phaseStart, phaseStart.plusSeconds(warmupSec), null, tp.getAccount().getFCMax() / 100 * IntensityZone.ENDURANCE.getMinHr())
        );
        phaseStart = phaseStart.plusSeconds(warmupSec);

        // Phase 2 : Corps de séance (Zone 2, ~70% FC max)
        plannedDataPoints.add(
                new PlannedDataPoint(phaseStart, phaseStart.plusSeconds(mainSec), null, tp.getAccount().getFCMax() / 100 * IntensityZone.TEMPO.getMinHr())
        );
        phaseStart = phaseStart.plusSeconds(mainSec);

        // Phase 3 : Retour au calme (Zone 1)
        plannedDataPoints.add(
                new PlannedDataPoint(phaseStart, end, null, tp.getAccount().getFCMax() / 100 * IntensityZone.ENDURANCE.getMinHr())
        );

        // Création de l’entraînement avec des PlannedDataPoints simples
        return new Workout(
                tp.getAccount(),
                sport,
                start,
                end,
                "AUTO_GENERATED_V1",
                TrainingStatus.PLANNED,
                plannedDataPoints
        );
    }


    private List<DailyPlan> generateWeeklyDailyPlans(TrainingPlan trainingPlan, LinkedHashMap<Sport, Integer> sportDistribution) {
        List<DailyPlan> dailyPlans = new ArrayList<>();
        List<DayOfWeek> daysOfWeek = trainingPlan.getDaysOfWeek();
        int totalDays = daysOfWeek.size();

        // Sort sports by number of sessions to plan (descending)
        List<Map.Entry<Sport, Integer>> sportsByFreq = new ArrayList<>(sportDistribution.entrySet());
        sportsByFreq.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Mark assigned days
        Map<Integer, Sport> assignedDays = new HashMap<>();
        Set<Integer> usedDayIndexes = new HashSet<>();

        for (Map.Entry<Sport, Integer> entry : sportsByFreq) {
            Sport sport = entry.getKey();
            int count = entry.getValue();

            if (count <= 0) continue;
            if (count > totalDays) {
                throw new IllegalArgumentException("Too many workouts to fit in available days.");
            }

            // Try to space evenly the sessions
            List<Integer> positions = new ArrayList<>();
            double interval = (double) totalDays / count;

            for (int i = 0; i < count; i++) {
                int idealIndex = (int) Math.round(i * interval);
                int idx = findClosestFreeDay(idealIndex, usedDayIndexes, totalDays);

                if (idx != -1) {
                    positions.add(idx);
                    usedDayIndexes.add(idx);
                    assignedDays.put(idx, sport);
                }
            }
        }

        // Ensure no same sport two days in a row (soft rule)
        List<Integer> sortedIndexes = new ArrayList<>(assignedDays.keySet());
        Collections.sort(sortedIndexes);

        for (int i = 1; i < sortedIndexes.size(); i++) {
            int prev = sortedIndexes.get(i - 1);
            int curr = sortedIndexes.get(i);
            if (curr - prev == 1 && assignedDays.get(prev).equals(assignedDays.get(curr))) {
                // Try to swap with a further unassigned day
                for (int j = totalDays - 1; j >= 0; j--) {
                    if (!usedDayIndexes.contains(j)) {
                        assignedDays.put(j, assignedDays.get(curr));
                        assignedDays.remove(curr);
                        usedDayIndexes.remove(curr);
                        usedDayIndexes.add(j);
                        break;
                    }
                }
            }
        }

        // Build the daily plans
        for (int i = 0; i < totalDays; i++) {
            if (assignedDays.containsKey(i)) {
                dailyPlans.add(new DailyPlan(daysOfWeek.get(i), assignedDays.get(i)));
            }
        }

        return dailyPlans;
    }

    /**
     * Finds the closest index to `target` that is not already used
     */
    private int findClosestFreeDay(int target, Set<Integer> used, int totalDays) {
        int radius = 0;
        while (radius < totalDays) {
            int lower = target - radius;
            int upper = target + radius;
            if (lower >= 0 && !used.contains(lower)) return lower;
            if (upper < totalDays && !used.contains(upper)) return upper;
            radius++;
        }
        return -1;
    }


    /**
     * Calcule la distribution des sports en reprenant ta logique initiale
     * (alternance running/cycling si nécessaire) et en s'assurant qu'on ne dépasse
     * pas nbOfWorkoutsPerWeek.
     */
    private LinkedHashMap<Sport, Integer> calculateSportDistribution(Integer nbOfWorkoutsPerWeek, List<Goal> goals) {
        LinkedHashMap<Sport, Integer> sportDistribution = new LinkedHashMap<>();

        for (Goal goal : goals) {
            Sport sport = goal.getSport();
            int nbWorkouts = goal.getNbOfWorkoutsPerWeek();
            sportDistribution.put(sport, sportDistribution.getOrDefault(sport, 0) + nbWorkouts);
        }

        if (sportDistribution.containsKey(Sport.RUNNING) && sportDistribution.containsKey(Sport.CYCLING)) {
            int running = sportDistribution.get(Sport.RUNNING);
            int cycling = sportDistribution.get(Sport.CYCLING);
            int swimming = sportDistribution.getOrDefault(Sport.SWIMMING, 0);

            int nbOfWorkoutsPerWeekWithoutSwimming = nbOfWorkoutsPerWeek - swimming;
            if (nbOfWorkoutsPerWeekWithoutSwimming < 0) nbOfWorkoutsPerWeekWithoutSwimming = 0;

            if (running + cycling > nbOfWorkoutsPerWeekWithoutSwimming) {
                sportDistribution.put(Sport.RUNNING, 0);
                sportDistribution.put(Sport.CYCLING, 0);
                for (int i = 0; i < nbOfWorkoutsPerWeekWithoutSwimming; i++) {
                    if (i % 2 == 0) {
                        sportDistribution.put(Sport.RUNNING, sportDistribution.getOrDefault(Sport.RUNNING, 0) + 1);
                    } else {
                        sportDistribution.put(Sport.CYCLING, sportDistribution.getOrDefault(Sport.CYCLING, 0) + 1);
                    }
                }
            }
        }

        return sportDistribution;
    }

    private Integer calculateNbOfWorkoutsPerWeek(List<Goal> goals, Account account) {
        Map<Sport, Integer> workoutPerSport = new HashMap<>();

        for (Goal goal : goals) {
            Sport sport = goal.getSport();
            int nbWorkouts = goal.getNbOfWorkoutsPerWeek();
            workoutPerSport.put(sport, workoutPerSport.getOrDefault(sport, 0) + nbWorkouts);
        }

        int running = workoutPerSport.getOrDefault(Sport.RUNNING, 0);
        int cycling = workoutPerSport.getOrDefault(Sport.CYCLING, 0);
        int swimming = workoutPerSport.getOrDefault(Sport.SWIMMING, 0);

        double fitnessLevelPonderation = (double) account.getLastFitnessLevel().getFitnessLevel() / 200 + 0.45;

        return (int) Math.round(
                (running + cycling) * fitnessLevelPonderation +
                        swimming
        );
    }

    private Integer calculateNbOfWeeks(TrainingPlan trainingPlan) {
        if (trainingPlan.getGoals().isEmpty()) {
            return 0;
        }

        return trainingPlan.getGoals().stream()
                .map(Goal::getNbOfWeek)
                .max(Integer::compareTo)
                .orElse(0);
    }
}
