package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.heigvd.dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.training_generator.TrainingGeneratorV1;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
/**
 * Service de gestion des plans d'entraînement.
 *
 * Permet la création de plans et la récupération du plan d'un utilisateur.
 */
public class TrainingPlanService {

    @Inject
    EntityManager em;

    @Inject
    TrainingGeneratorV1 trainingGeneratorV1;

    /**
     * Crée un plan d'entraînement après vérification d'unicité pour l'utilisateur.
     * @param tp plan d'entraînement à créer
     * @throws IllegalStateException si un plan existe déjà pour l'utilisateur
     */
    public void create(TrainingPlan tp) {
        // we need to check if the user already has a training plan
        Optional<TrainingPlan> existingPlan = getMyTrainingPlan(tp.getAccount().getId());
        if (existingPlan.isPresent()) {
            throw new IllegalStateException("User already has a training plan.");
        }
        em.persist(tp);
    }

    /**
     * Génère et crée un plan d'entraînement à partir des paramètres et du compte.
     * @param trainingPlanRequestDto paramètres de génération
     * @param account compte utilisateur
     */
    public void create(TrainingPlanRequestDto trainingPlanRequestDto, Account account) {
        TrainingPlan tp = trainingGeneratorV1.generateTrainingPlan(trainingPlanRequestDto, account);
        create(tp);
    }

    /**
     * Récupère le plan d'entraînement de l'utilisateur s'il existe.
     * @param accountId identifiant de l'utilisateur
     * @return Optional<TrainingPlan>
     */
    public Optional<TrainingPlan> getMyTrainingPlan(UUID accountId) {
        return em.createQuery("SELECT tp FROM TrainingPlan tp WHERE tp.account.id = :accountId", TrainingPlan.class)
                .setParameter("accountId", accountId)
                .getResultStream()
                .findFirst();
    }
}
