package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.bytebuddy.asm.Advice;
import org.heigvd.dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.training_generator.TrainingGeneratorV1;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TrainingPlanService {

    @Inject
    EntityManager em;

    @Inject
    TrainingGeneratorV1 trainingGeneratorV1;

    public void create(TrainingPlan tp) {
        // we need to check if the user already has a training plan
        Optional<TrainingPlan> existingPlan = getMyTrainingPlan(tp.getAccount().getId());
        if (existingPlan.isPresent()) {
            throw new IllegalStateException("User already has a training plan.");
        }
        em.persist(tp);
    }

    public void create(TrainingPlanRequestDto trainingPlanRequestDto, Account account) {
        TrainingPlan tp = trainingGeneratorV1.generateTrainingPlan(trainingPlanRequestDto, account);
        create(tp);
    }

    public Optional<TrainingPlan> getMyTrainingPlan(UUID accountId) {
        return em.createQuery("SELECT tp FROM TrainingPlan tp WHERE tp.account.id = :accountId", TrainingPlan.class)
                .setParameter("accountId", accountId)
                .getResultStream()
                .findFirst();
    }

    public Integer getCurrentWeekNb(TrainingPlan tp) {
        if (tp.getStartDate() == null || tp.getEndDate() == null) {
            return null; // Training plan dates are not set
        }
        if (tp.getStartDate().isAfter(tp.getEndDate())) {
            return null; // Invalid training plan dates
        }

        LocalDate currentDate = LocalDate.now();
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(tp.getStartDate(), currentDate);
        return (int) weeksBetween + 1; // +1 to count the first week as week 1
    }

    public Integer getCurrentWeekNbForUser(UUID accountId) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        return tp.map(this::getCurrentWeekNb).orElse(null);
    }

    public Integer getNbWorkoutsPerWeek(UUID accountId) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        // Check if current date is within the training plan period
        if (tp.isEmpty()) {
            return null; // No training plan found for the user
        }
        if (tp.get().getStartDate() == null || tp.get().getEndDate() == null) {
            return null; // Training plan dates are not set
        }
        if (tp.get().getStartDate().isAfter(tp.get().getEndDate())) {
            return null; // Invalid training plan dates
        }

        // If the current date is before the start date of the training plan, return the nb
        // of training from the first week
        if(LocalDate.now().isBefore(tp.get().getStartDate())) {
            return tp.get().getWeeklyPlans().getFirst().getDailyPlans().size();
        } else if(LocalDate.now().isBefore(tp.get().getEndDate()) && LocalDate.now().isAfter(tp.get().getStartDate())) {
            // Find the correct week based on the current date
            int indexCurrentWeeklyPlan = getCurrentWeekNb(tp.get()) - 1;
            return tp.get().getWeeklyPlans().get(indexCurrentWeeklyPlan).getDailyPlans().size();
        } else {
            throw new IllegalStateException("We can't get the number of workouts.");
        }

    }
}
