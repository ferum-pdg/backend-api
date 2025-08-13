package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.heigvd.entity.TrainingPlan.TrainingPlan;

@ApplicationScoped
public class TrainingPlanService {

    @Inject
    EntityManager em;

    public void create(TrainingPlan tp) {
        em.persist(tp);
    }
}
