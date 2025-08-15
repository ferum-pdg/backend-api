package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GoalService {

    @Inject
    EntityManager em;

    public Goal getSpecificGoal(Sport sport, Double targetDistance) {
        return em.createQuery("SELECT g FROM Goal g WHERE g.sport = :sport AND g.targetDistance = :targetDistance", Goal.class)
                .setParameter("sport", sport)
                .setParameter("targetDistance", targetDistance)
                .getSingleResult();
    }

    public List<Goal> getGoalsByIds(List<UUID> goalIds) {
        return em.createQuery("SELECT g FROM Goal g WHERE g.id IN :goalIds", Goal.class)
                .setParameter("goalIds", goalIds)
                .getResultList();
    }

    public Goal getGoalById(UUID id) {
        return em.find(Goal.class, id);
    }
}
