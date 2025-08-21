package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
/**
 * Service d'accès aux objectifs d'entraînement (Goals).
 */
public class GoalService {

    @Inject
    EntityManager em;

    /**
     * Récupère un objectif pour un sport et une distance cible donnée.
     * @param sport sport visé
     * @param targetDistance distance cible
     * @return objectif correspondant
     */
    public Goal getSpecificGoal(Sport sport, Double targetDistance) {
        return em.createQuery("SELECT g FROM Goal g WHERE g.sport = :sport AND g.targetDistance = :targetDistance", Goal.class)
                .setParameter("sport", sport)
                .setParameter("targetDistance", targetDistance)
                .getSingleResult();
    }

    /**
     * Récupère une liste d'objectifs par identifiants.
     * @param goalIds identifiants des objectifs
     * @return liste des objectifs
     */
    public List<Goal> getGoalsByIds(List<UUID> goalIds) {
        return em.createQuery("SELECT g FROM Goal g WHERE g.id IN :goalIds", Goal.class)
                .setParameter("goalIds", goalIds)
                .getResultList();
    }

    /**
     * Récupère un objectif par identifiant.
     * @param id identifiant de l'objectif
     * @return objectif
     */
    public Goal getGoalById(UUID id) {
        return em.find(Goal.class, id);
    }
}
