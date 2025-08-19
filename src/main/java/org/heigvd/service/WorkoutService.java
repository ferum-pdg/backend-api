package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.dto.WorkoutDto.WorkoutUploadDto;
import org.heigvd.entity.*;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.entity.Workout.WorkoutDetails;
import org.heigvd.entity.Workout.WorkoutStatus;
import org.hibernate.jdbc.Work;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class WorkoutService {

    @Inject
    EntityManager em;

    public Optional<Workout> findById(UUID id) {
        try {
            Workout workout = em.find(Workout.class, id);
            return Optional.ofNullable(workout);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Workout> findByAccountId(UUID accountId) {
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
    public Workout create(Workout workout) {

        em.persist(workout);
        return workout;
    }

    @Transactional
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
        return em.createQuery(
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
    }

    @Transactional
    public Workout createWorkoutOutOfTP(Account account, WorkoutUploadDto workout) {
        Workout newWorkout = new Workout();
        newWorkout.setAccount(account);
        newWorkout.setSport(Sport.valueOf(workout.getSport().toUpperCase()));
        newWorkout.setStartTime(workout.getStart());
        newWorkout.setEndTime(workout.getEnd());
        newWorkout.setStatus(WorkoutStatus.COMPLETED);

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

        // Update other fields as necessary
        // For example, if you have a list of data points to update:
        // existingWorkout.setDataPoints(workout.getDataPoints());

        em.merge(existingWorkout);

        return existingWorkout;
    }
}