package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.entity.*;
import org.heigvd.entity.Workout.Workout;

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

    /**
     * Recherche un workout par identifiant.
     * @param id identifiant du workout
     * @return Optional<Workout>
     */
    public Optional<Workout> findById(UUID id) {
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
                        "SELECT w FROM Workout w WHERE w.account.id = :accountId ORDER BY w.startTime DESC",
                        Workout.class)
                .setParameter("accountId", accountId)
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
}