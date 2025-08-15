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
        // we need to check if the user already has a training plan
        Optional<TrainingPlan> existingPlan = getMyTrainingPlan(tp.getAccount().getId());
        if (existingPlan.isPresent()) {
            throw new IllegalStateException("User already has a training plan.");
        }
        em.persist(tp);
    }

    public Optional<TrainingPlan> getMyTrainingPlan(UUID accountId) {
        return em.createQuery("SELECT tp FROM TrainingPlan tp WHERE tp.account.id = :accountId", TrainingPlan.class)
                .setParameter("accountId", accountId)
                .getResultStream()
                .findFirst();
    }
}
