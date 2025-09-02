package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;

import java.util.List;
import java.util.UUID;

/**
 * Service for accessing training goals (Goals).
 */
@ApplicationScoped
public class GoalService {

    @Inject
    EntityManager em;

    /**
     * Retrieves a goal for a given sport and target distance.
     * @param sport target sport
     * @param targetDistance target distance
     * @return corresponding goal
     */
    public Goal getSpecificGoal(Sport sport, Double targetDistance) {
        return em.createQuery("SELECT g FROM Goal g WHERE g.sport = :sport AND g.targetDistance = :targetDistance", Goal.class)
                .setParameter("sport", sport)
                .setParameter("targetDistance", targetDistance)
                .getSingleResult();
    }

    /**
     * Retrieves a list of goals by identifiers.
     * @param goalIds goal identifiers
     * @return list of goals
     */
    public List<Goal> getGoalsByIds(List<UUID> goalIds) {
        return em.createQuery("SELECT g FROM Goal g WHERE g.id IN :goalIds", Goal.class)
                .setParameter("goalIds", goalIds)
                .getResultList();
    }

    /**
     * Retrieves a goal by identifier.
     * @param id goal identifier
     * @return goal
     */
    public Goal getGoalById(UUID id) {
        return em.find(Goal.class, id);
    }

    /**
     * Retrieves goals filtered by sport.
     * @param sport target sport
     * @return list of goals for the specified sport
     */
    public List<Goal> getGoalsBySport(Sport sport) {
        return em.createQuery("SELECT g FROM Goal g WHERE g.sport IN :sport", Goal.class)
                .setParameter("sport", sport)
                .getResultList();
    }

    /**
     * Retrieves all available goals.
     * @return list of all goals
     */
    public List<Goal> getAllGoals() {
        return em.createQuery("SELECT g FROM Goal g", Goal.class)
                .getResultList();
    }
}