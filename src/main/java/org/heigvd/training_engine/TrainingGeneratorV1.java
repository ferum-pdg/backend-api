package org.heigvd.training_engine;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.*;
import org.heigvd.entity.TrainingPlan.DailyPlan;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.Workout.Workout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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

        HashMap<Sport, Integer> sportDistribution = calculateSportDistribution(nbOfWorkoutsPerWeek, goals);

        generateDailyPlans(trainingPlan, sportDistribution);

        return null;
    }

    private void generateDailyPlans(TrainingPlan trainingPlan, HashMap<Sport, Integer> sportDistribution) {
        List<DailyPlan> dailyPlans = new ArrayList<>();
        List<DayOfWeek> daysOfWeek = trainingPlan.getDaysOfWeek();

        // I want to distribute the sports evenly across the days of the week
        int dayIndex = 0;
        int sportIndex = 0;

        int nbOfSports = sportDistribution.size();
        int nbOfDays = daysOfWeek.size();

        // While there are still training to distribute, we go through available days and distribute the sports
        // with an decalage of the number of sports so the number of sports is distributed evenly across the days of the week.
        while (sportDistribution.values().stream().anyMatch(count -> count > 0)) {
            // If the current sport has no more workouts to distribute, we move to the next sport
            while (sportIndex < nbOfSports && sportDistribution.get(Sport.values()[sportIndex]) <= 0) {
                sportIndex++;
            }

            // If we have reached the end of the sports list, we reset the index
            if (sportIndex >= nbOfSports) {
                sportIndex = 0;
            }

            // If we have a valid sport to distribute
            if (sportIndex < nbOfSports) {
                Sport currentSport = Sport.values()[sportIndex];
                dailyPlans.add(new DailyPlan(daysOfWeek.get(dayIndex), currentSport));
                sportDistribution.put(currentSport, sportDistribution.get(currentSport) - 1);
            }

            // Increment day index and wrap around if necessary
            dayIndex = (dayIndex + 3) % nbOfDays;

            // Increment sport index and wrap around if necessary
            sportIndex = (sportIndex + 1) % nbOfSports;
        }

        /* While all elements in sportDistribution is not equals to 0, we will add the sport to the daily plans
        while (sportDistribution.values().stream().anyMatch(count -> count > 0)) {
            // go trough trainingPlan.getDaysOfWeek() and add the sport to the daily plans
            for (int i = 0; i < nbOfDays; i += nbOfSports) {
                DayOfWeek dayOfWeek = daysOfWeek.get(dayIndex);
                dailyPlans.add(new DailyPlan(dayOfWeek, Sport.values()[sportIndex]));
                sportDistribution.put(Sport.values()[sportIndex], sportDistribution.get(Sport.values()[sportIndex]) - 1);
                dayIndex = (dayIndex + 1) % nbOfDays; // Increment day index and wrap around if necessary
                sportIndex = (sportIndex + 1) % nbOfSports; // Increment sport
            }
        }*/

        trainingPlan.setPairWeeklyPlans(dailyPlans);
    }

    /**
     * Calculates the distribution of sports based on the goals and the number of workouts per week.
     * @param nbOfWorkoutsPerWeek the total number of workouts per week
     * @param goals the list of goals for the training plan
     * @return a map with the sport as key and the number of workouts as value
     */
    private HashMap<Sport, Integer> calculateSportDistribution(Integer nbOfWorkoutsPerWeek, List<Goal> goals) {
        HashMap<Sport, Integer> sportDistribution = new HashMap<>();

        for (Goal goal : goals) {
            Sport sport = goal.getSport();
            int nbWorkouts = goal.getNbOfWorkoutsPerWeek();
            sportDistribution.put(sport, sportDistribution.getOrDefault(sport, 0) + nbWorkouts);
        }

        if (sportDistribution.containsKey(Sport.RUNNING) && sportDistribution.containsKey(Sport.CYCLING)) {

            // If both running and cycling are present, we need to adjust the distribution
            int running = sportDistribution.get(Sport.RUNNING);
            int cycling = sportDistribution.get(Sport.CYCLING);

            int nbOfWorkoutsPerWeekWithoutSwimming = nbOfWorkoutsPerWeek - sportDistribution.getOrDefault(Sport.SWIMMING, 0);

            if (running + cycling > nbOfWorkoutsPerWeekWithoutSwimming) {
                // If the total exceeds the number of workouts per week, we need to scale down
                double scalingFactor = (double) nbOfWorkoutsPerWeekWithoutSwimming / (running + cycling);
                sportDistribution.put(Sport.RUNNING, (int) Math.round(running * scalingFactor));
                sportDistribution.put(Sport.CYCLING, (int) Math.round(cycling * scalingFactor));
            }
        }

        return sportDistribution;
    }

    /**
     * Calculates the number of workouts per week based on the goals and use a mathematical formula to take to account
     * the shared effect of EF for cycling and running.
     * @param goals the list of goals for the training plan
     * @return the total number of workouts per week
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
                swimming
        );
    }

    /**
     * Calculates the number of weeks needed for the training plan based on the goals.
     * @param trainingPlan the training plan to analyze
     * @return the number of weeks needed for the training plan
     */
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
