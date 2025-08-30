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
 * Service responsable de la mise à jour intelligente du niveau de fitness
 * basé sur l'analyse des performances des entraînements
 */
@ApplicationScoped
public class FitnessLevelService {

    @Inject
    EntityManager em;

    @Inject
    AccountService accountService;

    /**
     * Met à jour le fitness level d'un utilisateur basé sur ses performances récentes
     * @param account Compte utilisateur
     * @param trigger Entraînement qui a déclenché l'évaluation
     * @param workoutGrade Note de l'entraînement déclencheur
     */
    @Transactional
    public void updateFitnessLevel(Account account, Workout trigger, double workoutGrade) {
        FitnessLevel currentLevel = account.getLastFitnessLevel();
        if (currentLevel == null) {
            // Créer un niveau initial basé sur le premier entraînement
            int initialLevel = estimateInitialFitnessLevel(trigger);
            FitnessLevel initialFitnessLevel = new FitnessLevel(LocalDate.now(), initialLevel);
            account.addFitnessLevel(initialFitnessLevel);
            accountService.update(account);
            return;
        }

        // Vérifier si une mise à jour est nécessaire
        if (!shouldUpdateFitnessLevel(currentLevel, trigger)) {
            return;
        }

        // Analyser les performances récentes
        FitnessAnalysis analysis = analyzeRecentPerformances(account, trigger.getStartTime().toLocalDate());

        // Calculer le nouveau niveau
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
     * Détermine s'il faut mettre à jour le fitness level
     */
    private boolean shouldUpdateFitnessLevel(FitnessLevel currentLevel, Workout trigger) {
        LocalDate lastUpdate = currentLevel.getDate();
        LocalDate workoutDate = trigger.getStartTime().toLocalDate();

        // Mise à jour minimum tous les 7 jours
        long daysSinceUpdate = ChronoUnit.DAYS.between(lastUpdate, workoutDate);

        return daysSinceUpdate >= 7;
    }

    /**
     * Analyse les performances récentes pour évaluer l'évolution
     */
    private FitnessAnalysis analyzeRecentPerformances(Account account, LocalDate currentDate) {
        // Récupérer les entraînements des 4 dernières semaines
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
     * Calcule le nouveau niveau de fitness
     */
    private int calculateNewFitnessLevel(int currentLevel, FitnessAnalysis analysis, double triggerGrade) {
        double adjustment = 0.0;

        // 1. Progression basée sur la régularité (30%)
        adjustment += analysis.getConsistencyScore() * 0.3;

        // 2. Progression basée sur l'intensité moyenne (25%)
        adjustment += analysis.getIntensityProgression() * 0.25;

        // 3. Progression basée sur le volume (20%)
        adjustment += analysis.getVolumeProgression() * 0.2;

        // 4. Performance récente (15%)
        adjustment += analysis.getPerformanceScore() * 0.15;

        // 5. Note de l'entraînement déclencheur (10%)
        adjustment += ((triggerGrade - 5.0) / 5.0) * 0.1;

        // Convertir l'ajustement en points de fitness level
        int levelAdjustment = (int) Math.round(adjustment * 10);

        // Limiter les changements drastiques
        levelAdjustment = Math.max(-5, Math.min(5, levelAdjustment));

        int newLevel = currentLevel + levelAdjustment;
        return Math.max(1, Math.min(100, newLevel));
    }

    /**
     * Estime le niveau initial basé sur le premier entraînement
     */
    private int estimateInitialFitnessLevel(Workout workout) {
        int baseLevel = 50; // Niveau moyen par défaut

        // Ajustement basé sur l'intensité
        if (workout.getAvgHeartRate() > 0) {
            double intensityRatio = (double) workout.getAvgHeartRate() / workout.getAccount().getFCMax();
            int durationMinutes = workout.getDurationSec() / 60;

            // Si capable de maintenir haute intensité longtemps = niveau plus élevé
            if (intensityRatio > 0.8 && durationMinutes > 45) {
                baseLevel += 20;
            } else if (intensityRatio > 0.7 && durationMinutes > 30) {
                baseLevel += 10;
            } else if (intensityRatio < 0.6 || durationMinutes < 20) {
                baseLevel -= 10;
            }
        }

        // Ajustement basé sur le sport (approximation de la difficulté)
        switch (workout.getSport()) {
            case SWIMMING -> baseLevel += 5; // Sport technique
            case CYCLING -> baseLevel += 0; // Sport de référence
            case RUNNING -> baseLevel -= 5; // Plus accessible
        }

        return Math.max(20, Math.min(80, baseLevel));
    }

    /**
     * Classe interne pour l'analyse de fitness
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
         * Score de régularité basé sur la fréquence des entraînements
         * @return Score entre -1.0 et 1.0
         */
        public double getConsistencyScore() {
            if (workouts.size() < 2) return -0.5;

            int weekCount = 4;
            int workoutsPerWeek = workouts.size() / weekCount;

            // Score optimal : 3-5 entraînements par semaine
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
         * Progression de l'intensité moyenne
         * @return Score entre -1.0 et 1.0
         */
        public double getIntensityProgression() {
            if (workouts.size() < 4) return 0.0;

            // Comparer la première moitié avec la seconde moitié
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

            // Normaliser entre -1 et 1
            return Math.max(-1.0, Math.min(1.0, progression * 10));
        }

        /**
         * Progression du volume d'entraînement
         * @return Score entre -1.0 et 1.0
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

            // Normaliser : progression de 20% = score de 1.0
            return Math.max(-1.0, Math.min(1.0, volumeProgression * 5));
        }

        /**
         * Score de performance basé sur les notes récentes
         * @return Score entre -1.0 et 1.0
         */
        public double getPerformanceScore() {
            if (workouts.isEmpty()) return 0.0;

            // Utiliser les grades si disponibles, sinon estimer
            double avgGrade = workouts.stream()
                    .filter(w -> w.getGrade() != null && w.getGrade() > 0)
                    .mapToDouble(Workout::getGrade)
                    .average()
                    .orElse(5.0); // Grade moyen par défaut

            // Convertir grade (0-10) en score (-1 à 1)
            return (avgGrade - 5.0) / 5.0;
        }
    }
}