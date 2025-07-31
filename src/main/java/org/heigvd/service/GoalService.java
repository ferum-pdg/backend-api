package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;

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
}
