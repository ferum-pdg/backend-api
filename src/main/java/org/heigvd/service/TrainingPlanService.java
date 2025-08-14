package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.heigvd.entity.TrainingPlan.TrainingPlan;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TrainingPlanService {

    @Inject
    EntityManager em;

    public void create(TrainingPlan tp) {
        em.persist(tp);
    }

    public Optional<TrainingPlan> getMyTrainingPlan(UUID accountId) {
        return em.createQuery("SELECT tp FROM TrainingPlan tp WHERE tp.account.id = :accountId", TrainingPlan.class)
                .setParameter("accountId", accountId)
                .getResultStream()
                .findFirst();
    }
}
