package org.heigvd.training_engine;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.*;
import org.heigvd.entity.Workout.Workout;

import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class TrainingGeneratorV1 implements TrainingGenerator {

    public List<Workout> generateTrainingWorkouts(TrainingPlan trainingPlan) {

        List<Goal> goals = trainingPlan.getGoals();

        int nbOfWorkoutsPerWeek = calculateNbOfWorkoutsPerWeek(goals, trainingPlan.getAccount());

        int nbOfTrainingWeeks = calculateNbOfWeeks(trainingPlan);

        // Set de startDate to the end date minus the number of training weeks
        LocalDate startDate = trainingPlan.getEndDate().minusWeeks(nbOfTrainingWeeks);

        if(nbOfWorkoutsPerWeek > trainingPlan.getDaysOfWeek().size()) {
            throw new IllegalArgumentException("Not enough available days to schedule the workouts.");
        }

        return new ArrayList<Workout>();
    }

    /**
     * Calculates the number of workouts per week based on the goals and use a mathematical formula to take to account
     * the shared effect of EF for cycling and running.
     * @param goals
     * @return
     */
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

        double fitnessLevelPonderation = (double) account.getLastFitnessLevel().getFitnessLevel() / 200 + 0.4;

        return (int) Math.round(
                (running + cycling) * fitnessLevelPonderation +
                (swimming > 1 ? swimming - 1 : swimming)
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

    /*
    public List<Workout> generateTrainingWorkouts(Account account, TrainingPlan trainingPlan) {
        List<Workout> workouts = new ArrayList<>();

        LocalDate currentDate = trainingPlan.getStartDate();
        LocalDate endDate = trainingPlan.getEndDate();
        List<DayOfWeek> availableDays = trainingPlan.getDaysOfWeek().stream()
                .sorted(Comparator.comparingInt(DayOfWeek::getValue))
                .toList();

        Goal goal = trainingPlan.getGoals().get(0);
        Sport sport = goal.getSport();
        int nbWorkoutsPerWeek = goal.getNbOfWorkoutsPerWeek();

        // Utilisé pour grouper les jours par semaine
        Map<Integer, List<LocalDate>> daysByWeek = new TreeMap<>();

        // Grouper tous les jours dispo par semaine
        while (!currentDate.isAfter(endDate)) {
            DayOfWeek dow = currentDate.getDayOfWeek();
            if (availableDays.contains(dow)) {
                int week = currentDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                daysByWeek.computeIfAbsent(week, w -> new ArrayList<>()).add(currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        // Pour chaque semaine, on choisit les jours espacés
        for (List<LocalDate> weekDays : daysByWeek.values()) {
            if (weekDays.isEmpty()) continue;

            int available = weekDays.size();
            int toSchedule = Math.min(nbWorkoutsPerWeek, available);

            if (toSchedule == available) {
                // Pas le choix, on les prend tous
                for (LocalDate date : weekDays) {
                    workouts.add(buildWorkout(account, trainingPlan, sport, date));
                }
            } else {
                // Répartition équilibrée
                double step = (double) (available - 1) / (toSchedule - 1);
                for (int i = 0; i < toSchedule; i++) {
                    int index = (int) Math.round(i * step);
                    LocalDate date = weekDays.get(index);
                    workouts.add(buildWorkout(account, trainingPlan, sport, date));
                }
            }
        }

        return workouts;
    }

    private Workout buildWorkout(Account account, TrainingPlan plan, Sport sport, LocalDate date) {
        OffsetDateTime start = date.atTime(18, 0).atOffset(ZoneOffset.UTC);
        OffsetDateTime end = start.plusHours(1);
        return new Workout(account, plan, sport, start, end, "GENERATOR_V1", TrainingStatus.PLANNED);
    }

    */
}
