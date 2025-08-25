package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.heigvd.dto.WorkoutDto.WorkoutUploadDto;
import org.heigvd.entity.*;
import org.heigvd.entity.Workout.Workout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
/**
 * Service de gestion des séances d'entraînement (Workouts).
 *
 * Permet la recherche, la création et la suppression de workouts.
 */
public class WorkoutService {

    @Inject
    EntityManager em;

    @Inject
    TrainingPlanService trainingPlanService;

    /**
     * Recherche un workout par identifiant.
     * @param id identifiant du workout
     * @return Optional<Workout>
     */
    public Optional<Workout> getWorkoutByID(UUID id) {
        try {
            Workout workout = em.find(Workout.class, id);
            return Optional.ofNullable(workout);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Liste les workouts d'un utilisateur, triés par date décroissante.
     * @param accountId identifiant du compte
     * @return liste des workouts
     */
    public List<Workout> findByAccountId(UUID accountId) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId " +
                                "ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    @Transactional
    public List<Workout> getCurrentWeekWorkouts(UUID accountId) {
        OffsetDateTime now = OffsetDateTime.now();
        // On prend le LocalDate courant
        LocalDate today = now.toLocalDate();
        // Début de semaine = lundi 00:00
        LocalDateTime startOfWeekLdt = today
                .with(java.time.DayOfWeek.MONDAY)
                .atStartOfDay();
        // Fin de semaine = dimanche 23:59:59
        LocalDateTime endOfWeekLdt = startOfWeekLdt.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        // Conversion en OffsetDateTime avec le même offset que "now"
        OffsetDateTime startOfWeek = startOfWeekLdt.atOffset(now.getOffset());
        OffsetDateTime endOfWeek   = endOfWeekLdt.atOffset(now.getOffset());

        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId " +
                                "AND w.startTime >= :startOfWeek AND w.startTime <= :endOfWeek " +
                                "ORDER BY w.startTime ASC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("startOfWeek", startOfWeek)
                .setParameter("endOfWeek", endOfWeek)
                .getResultList();
    }
  
    /**
     * Liste les workouts d'un utilisateur pour un sport donné.
     * @param accountId identifiant du compte
     * @param sport sport ciblé
     * @return liste des workouts filtrés
     */
    public List<Workout> findByAccountIdAndSport(UUID accountId, Sport sport) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.sport = :sport " +
                                "ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("sport", sport)
                .getResultList();
    }

    public List<Workout> getNextNWorkouts(UUID accountId) {
        Optional<TrainingPlan> tp = trainingPlanService.getMyTrainingPlan(accountId);
        if(tp.isEmpty()) {
            return getAllWorkouts(accountId);
        } else {
            Integer nbWorkouts = trainingPlanService.getNbWorkoutsPerWeek(accountId);
            // return the next nbWorkouts for the user based on the current date
            return em.createQuery(
                            "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.startTime >= :startTime " +
                                    "ORDER BY w.startTime ASC",
                            Workout.class)
                    .setParameter("accountId", accountId)
                    .setParameter("startTime", OffsetDateTime.now())
                    .setMaxResults(nbWorkouts)
                    .getResultList();
        }
    }

    public List<Workout> getAllWorkouts(UUID accountId) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    public List<Workout> findByAccountIdAndSport(UUID accountId, Sport sport) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.sport = :sport ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("sport", sport)
                .getResultList();
    }

    @Transactional
    /**
     * Crée un nouveau workout.
     * @param workout entité workout à persister
     * @return le workout créé
     */
    public Workout create(Workout workout) {

        em.persist(workout);
        return workout;
    }

    @Transactional
    /**
     * Supprime un workout par identifiant.
     * @param id identifiant du workout
     * @return true si supprimé, false sinon
     */
    public boolean delete(UUID id) {
        try {
            Workout workout = em.find(Workout.class, id);
            if (workout != null) {
                em.remove(workout);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }


    public Optional<Workout> findClosestWorkout(WorkoutUploadDto workout, Account account) {
        Sport sport = Sport.valueOf(workout.getSport().toUpperCase());
        // Find workouts that are the same day, sport
        OffsetDateTime startOfDay = workout.getStart().toLocalDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime endOfDay = workout.getEnd().toLocalDate().atTime(23, 59, 59).atOffset(OffsetDateTime.now().getOffset());
        Optional<Workout> getWorkout = em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.sport = :sport " +
                                "AND w.startTime >= :startOfDay AND w.endTime <= :endOfDay " +
                                "ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", account.getId())
                .setParameter("sport", sport)
                .setParameter("startOfDay", startOfDay)
                .setParameter("endOfDay", endOfDay)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();

        return getWorkout;
    }

    @Transactional
    public Workout createWorkoutOutOfTP(Account account, WorkoutUploadDto workout) {
        Workout newWorkout = new Workout();
        newWorkout.setAccount(account);
        newWorkout.setSport(Sport.valueOf(workout.getSport().toUpperCase()));
        newWorkout.setStartTime(workout.getStart());
        newWorkout.setEndTime(workout.getEnd());
        newWorkout.setStatus(WorkoutStatus.COMPLETED);
        newWorkout.setDistanceMeters(workout.getDistance());
        newWorkout.setCaloriesKcal(workout.getCaloriesKcal());
        newWorkout.setAvgHeartRate(workout.getAvgBPM().intValue());
        newWorkout.setMaxHeartRate(workout.getMaxBPM().intValue());
        newWorkout.setSource(workout.getSource());
        newWorkout.setDurationSec((int) (newWorkout.getEndTime().toEpochSecond() - newWorkout.getStartTime().toEpochSecond()));
        newWorkout.setAvgSpeed(workout.getAvgSpeed());

        newWorkout.setActualBPMDataPoints(workout.getBpmDataPoints());
        newWorkout.setActualSpeedDataPoints(workout.getSpeedDataPoints());

        em.persist(newWorkout);

        return newWorkout;
    }

    @Transactional
    public Workout mergeWorkoutWithExisting(Workout existingWorkout, WorkoutUploadDto workout) {
        existingWorkout.setSport(Sport.valueOf(workout.getSport().toUpperCase()));
        existingWorkout.setStartTime(workout.getStart());
        existingWorkout.setEndTime(workout.getEnd());
        existingWorkout.setStatus(WorkoutStatus.COMPLETED);
        existingWorkout.setDistanceMeters(workout.getDistance());
        existingWorkout.setCaloriesKcal(workout.getCaloriesKcal());
        existingWorkout.setAvgHeartRate(workout.getAvgBPM().intValue());
        existingWorkout.setMaxHeartRate(workout.getMaxBPM().intValue());
        existingWorkout.setSource(workout.getSource());
        existingWorkout.setDurationSec((int) (existingWorkout.getEndTime().toEpochSecond() - existingWorkout.getStartTime().toEpochSecond()));
        existingWorkout.setAvgSpeed(workout.getAvgSpeed());

        existingWorkout.setActualBPMDataPoints(workout.getBpmDataPoints());
        existingWorkout.setActualSpeedDataPoints(workout.getSpeedDataPoints());

        em.merge(existingWorkout);

        return existingWorkout;
    }
}