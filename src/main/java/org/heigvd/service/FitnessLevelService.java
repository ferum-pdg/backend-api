package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.entity.Account;
import org.heigvd.entity.FitnessLevel;
import org.heigvd.entity.Sport;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for intelligent fitness level updates
 * based on workout performance analysis.
 */
@ApplicationScoped
public class FitnessLevelService {

    @Inject
    EntityManager em;

    @Inject
    AccountService accountService;

    /**
     * Updates a user's fitness level based on their recent performances.
     * @param account User account
     * @param trigger Workout that triggered the evaluation
     * @param workoutGrade Grade of the trigger workout
     */
    @Transactional
    public void updateFitnessLevel(Account account, Workout trigger, double workoutGrade) {
        FitnessLevel currentLevel = account.getLastFitnessLevel();
        if (currentLevel == null) {
            int initialLevel = estimateInitialFitnessLevel(trigger);
            FitnessLevel initialFitnessLevel = new FitnessLevel(LocalDate.now(), initialLevel);
            account.addFitnessLevel(initialFitnessLevel);
            accountService.update(account);
            return;
        }

        if (!shouldUpdateFitnessLevel(currentLevel, trigger)) {
            return;
        }

        FitnessAnalysis analysis = analyzeRecentPerformances(account, trigger.getStartTime().toLocalDate());

        int newLevel = calculateNewFitnessLevel(currentLevel.getFitnessLevel(), analysis, workoutGrade);

        if (newLevel != currentLevel.getFitnessLevel()) {
            FitnessLevel newFitnessLevel = new FitnessLevel(trigger.getStartTime().toLocalDate(), newLevel);
            account.addFitnessLevel(newFitnessLevel);
            accountService.update(account);

            System.out.printf("Fitness level updated: %s -> %d (was %d)%n",
                    account.getEmail(), newLevel, currentLevel.getFitnessLevel());
        }
    }

    /**
     * Determines whether the fitness level should be updated.
     */
    private boolean shouldUpdateFitnessLevel(FitnessLevel currentLevel, Workout trigger) {
        LocalDate lastUpdate = currentLevel.getDate();
        LocalDate workoutDate = trigger.getStartTime().toLocalDate();

        long daysSinceUpdate = ChronoUnit.DAYS.between(lastUpdate, workoutDate);

        return daysSinceUpdate >= 7;
    }

    /**
     * Analyzes recent performances to evaluate progress.
     */
    private FitnessAnalysis analyzeRecentPerformances(Account account, LocalDate currentDate) {
        LocalDate startDate = currentDate.minusWeeks(4);

        List<Workout> recentWorkouts = em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId " +
                                "AND w.startTime >= :startDate AND w.startTime <= :endDate " +
                                "AND w.status = :status ORDER BY w.startTime DESC", Workout.class)
                .setParameter("accountId", account.getId())
                .setParameter("startDate", startDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC))
                .setParameter("endDate", currentDate.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.UTC))
                .setParameter("status", WorkoutStatus.COMPLETED)
                .getResultList();

        return new FitnessAnalysis(recentWorkouts, account);
    }

    /**
     * Calculates the new fitness level.
     */
    private int calculateNewFitnessLevel(int currentLevel, FitnessAnalysis analysis, double triggerGrade) {
        double adjustment = 0.0;

        adjustment += analysis.getConsistencyScore() * 0.3;

        adjustment += analysis.getIntensityProgression() * 0.25;

        adjustment += analysis.getVolumeProgression() * 0.2;

        adjustment += analysis.getPerformanceScore() * 0.15;

        adjustment += ((triggerGrade - 5.0) / 5.0) * 0.1;

        int levelAdjustment = (int) Math.round(adjustment * 10);

        levelAdjustment = Math.max(-5, Math.min(5, levelAdjustment));

        int newLevel = currentLevel + levelAdjustment;
        return Math.max(1, Math.min(100, newLevel));
    }

    /**
     * Estimates the initial level based on the first workout.
     */
    private int estimateInitialFitnessLevel(Workout workout) {
        int baseLevel = 50;

        if (workout.getAvgHeartRate() > 0) {
            double intensityRatio = (double) workout.getAvgHeartRate() / workout.getAccount().getFCMax();
            int durationMinutes = workout.getDurationSec() / 60;

            if (intensityRatio > 0.8 && durationMinutes > 45) {
                baseLevel += 20;
            } else if (intensityRatio > 0.7 && durationMinutes > 30) {
                baseLevel += 10;
            } else if (intensityRatio < 0.6 || durationMinutes < 20) {
                baseLevel -= 10;
            }
        }

        switch (workout.getSport()) {
            case SWIMMING -> baseLevel += 5;
            case CYCLING -> baseLevel += 0;
            case RUNNING -> baseLevel -= 5;
        }

        return Math.max(20, Math.min(80, baseLevel));
    }

    /**
     * Inner class for fitness analysis.
     */
    private static class FitnessAnalysis {
        private final List<Workout> workouts;
        private final Account account;
        private final Map<Sport, List<Workout>> workoutsBySport;

        public FitnessAnalysis(List<Workout> workouts, Account account) {
            this.workouts = workouts;
            this.account = account;
            this.workoutsBySport = workouts.stream()
                    .collect(Collectors.groupingBy(Workout::getSport));
        }

        /**
         * Consistency score based on workout frequency.
         * @return Score between -1.0 and 1.0
         */
        public double getConsistencyScore() {
            if (workouts.size() < 2) return -0.5;

            int weekCount = 4;
            int workoutsPerWeek = workouts.size() / weekCount;

            if (workoutsPerWeek >= 3 && workoutsPerWeek <= 5) {
                return 1.0;
            } else if (workoutsPerWeek >= 2 && workoutsPerWeek <= 6) {
                return 0.7;
            } else if (workoutsPerWeek >= 1 && workoutsPerWeek <= 7) {
                return 0.3;
            } else {
                return -0.3;
            }
        }

        /**
         * Average intensity progression.
         * @return Score between -1.0 and 1.0
         */
        public double getIntensityProgression() {
            if (workouts.size() < 4) return 0.0;

            int halfSize = workouts.size() / 2;
            List<Workout> firstHalf = workouts.subList(0, halfSize);
            List<Workout> secondHalf = workouts.subList(halfSize, workouts.size());

            double avgIntensityFirst = firstHalf.stream()
                    .mapToDouble(w -> (double) w.getAvgHeartRate() / account.getFCMax())
                    .average().orElse(0.0);

            double avgIntensitySecond = secondHalf.stream()
                    .mapToDouble(w -> (double) w.getAvgHeartRate() / account.getFCMax())
                    .average().orElse(0.0);

            double progression = avgIntensitySecond - avgIntensityFirst;

            return Math.max(-1.0, Math.min(1.0, progression * 10));
        }

        /**
         * Training volume progression.
         * @return Score between -1.0 and 1.0
         */
        public double getVolumeProgression() {
            if (workouts.size() < 4) return 0.0;

            int halfSize = workouts.size() / 2;
            List<Workout> firstHalf = workouts.subList(0, halfSize);
            List<Workout> secondHalf = workouts.subList(halfSize, workouts.size());

            double avgDurationFirst = firstHalf.stream()
                    .mapToInt(Workout::getDurationSec)
                    .average().orElse(0.0);

            double avgDurationSecond = secondHalf.stream()
                    .mapToInt(Workout::getDurationSec)
                    .average().orElse(0.0);

            if (avgDurationFirst == 0) return 0.0;

            double volumeProgression = (avgDurationSecond - avgDurationFirst) / avgDurationFirst;

            return Math.max(-1.0, Math.min(1.0, volumeProgression * 5));
        }

        /**
         * Performance score based on recent grades.
         * @return Score between -1.0 and 1.0
         */
        public double getPerformanceScore() {
            if (workouts.isEmpty()) return 0.0;

            double avgGrade = workouts.stream()
                    .filter(w -> w.getGrade() != null && w.getGrade() > 0)
                    .mapToDouble(Workout::getGrade)
                    .average()
                    .orElse(5.0);

            return (avgGrade - 5.0) / 5.0;
        }
    }
}