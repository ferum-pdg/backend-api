package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.entity.*;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.entity.Workout.WorkoutStatus;

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

    /*
    public List<Workout> findByTrainingPlan(UUID accountId, UUID trainingPlanId) {
        return em.createQuery(
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId AND w.trainingPlan.id = :trainingPlanId ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
                .setParameter("trainingPlanId", trainingPlanId)
                .getResultList();
    }
    */

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
}