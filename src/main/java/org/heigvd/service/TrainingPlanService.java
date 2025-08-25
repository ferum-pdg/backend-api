package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.training_generator.TrainingGeneratorV1;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
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

    public boolean checkIfItsLastWeek(UUID accountId, Integer weekNb) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        return tp.filter(trainingPlan -> weekNb.equals(trainingPlan.getWeeklyPlans().size())).isPresent();

    }

    public boolean checkIfLastWeek(UUID accountId) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        if (tp.isEmpty()) {
            return false;
        }
        Integer currentWeek = getCurrentWeekNb(tp.get());
        return currentWeek != null && currentWeek.equals(tp.get().getWeeklyPlans().size());
    }

    public Integer getWeekNumberForDate(TrainingPlan tp, LocalDate date) {
        if (tp.getStartDate() == null || tp.getEndDate() == null) {
            return null; // Training plan dates are not set
        }
        if (tp.getStartDate().isAfter(tp.getEndDate())) {
            return null; // Invalid training plan dates
        }
        if (date.isBefore(tp.getStartDate()) || date.isAfter(tp.getEndDate())) {
            return null; // Date is outside the training plan period
        }

        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(tp.getStartDate(), date);
        return (int) weeksBetween + 1; // +1 to count the first week as week 1
    }

    public List<OffsetDateTime> getDatesForNextWorkouts(UUID accountId) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        if (tp.isEmpty()) {
            return List.of();
        }

        List<OffsetDateTime> dates = new ArrayList<>();
        // Add the date of the current week Monday
        dates.add(OffsetDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .with(LocalTime.MIDNIGHT));

        if(checkIfLastWeek(accountId)) {
            dates.add(OffsetDateTime.now()
                    .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                    .with(LocalTime.MAX));
        } else {
            dates.add(OffsetDateTime.now()
                    .plusWeeks(1)
                    .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                    .with(LocalTime.MAX));
        }

        return dates;
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
