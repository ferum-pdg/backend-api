package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.training_plan.WeeklyPlan;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for training plan management and operations.
 *
 * Provides methods for creating, retrieving, and managing training plans,
 * including week calculations and date-based operations.
 */
@ApplicationScoped
public class TrainingPlanService {

    @Inject
    EntityManager em;

    @Inject
    TrainingGeneratorService tgs;

    /**
     * Generates a new training plan for the user.
     * @param request DTO containing generation parameters
     * @param account user account
     * @return generated TrainingPlan
     */
    @Transactional
    public TrainingPlan generateTrainingPlan(TrainingPlanRequestDto request, Account account) {
        return tgs.generate(request, account);
    }

    /**
     * Creates a training plan after checking uniqueness for the user.
     * @param tp training plan to create
     * @throws IllegalStateException if a plan already exists for the user
     */
    @Transactional
    public void create(TrainingPlan tp) {
        Optional<TrainingPlan> existingPlan = getMyTrainingPlan(tp.getAccount().getId());
        if (existingPlan.isPresent()) {
            throw new IllegalStateException("User already has a training plan.");
        }
        em.persist(tp);
    }

    /**
     * Generates and persists a new training plan for the user.
     * @param request DTO containing generation parameters
     * @param account user account
     * @return created and persisted TrainingPlan
     * @throws IllegalStateException if a plan already exists for the user
     */
    @Transactional
    public TrainingPlan generateAndCreate(TrainingPlanRequestDto request, Account account) {
        Optional<TrainingPlan> existingPlan = getMyTrainingPlan(account.getId());
        if (existingPlan.isPresent()) {
            throw new IllegalStateException("User already has a training plan.");
        }

        TrainingPlan newPlan = tgs.generate(request, account);

        em.persist(newPlan);

        return newPlan;
    }

    /**
     * Merges an existing training plan with updates.
     * @param tp training plan to merge
     */
    @Transactional
    public void merge(TrainingPlan tp) {
        em.merge(tp);
    }

    /**
     * Replaces the existing training plan with a new one.
     * @param request DTO containing generation parameters
     * @param account user account
     * @return new created TrainingPlan
     */
    @Transactional
    public TrainingPlan replaceTrainingPlan(TrainingPlanRequestDto request, Account account) {
        Optional<TrainingPlan> existingPlan = getMyTrainingPlan(account.getId());
        existingPlan.ifPresent(plan -> em.remove(plan));

        TrainingPlan newPlan = tgs.generate(request, account);
        em.persist(newPlan);

        return newPlan;
    }

    /**
     * Retrieves the user's training plan if it exists.
     * @param accountId user identifier
     * @return Optional<TrainingPlan>
     */
    public Optional<TrainingPlan> getMyTrainingPlan(UUID accountId) {
        return em.createQuery("SELECT tp FROM TrainingPlan tp WHERE tp.account.id = :accountId", TrainingPlan.class)
                .setParameter("accountId", accountId)
                .getResultStream()
                .findFirst();
    }

    /**
     * Retrieves the user's current active training plan.
     * @param accountId user identifier
     * @return Optional<TrainingPlan> that is currently active
     */
    public Optional<TrainingPlan> getMyCurrentTrainingPlan(UUID accountId) {
        LocalDate currentDate = LocalDate.now();
        return em.createQuery("SELECT tp FROM TrainingPlan tp WHERE tp.account.id = :accountId AND tp.startDate <= :currentDate AND tp.endDate >= :currentDate", TrainingPlan.class)
                .setParameter("accountId", accountId)
                .setParameter("currentDate", currentDate)
                .getResultStream()
                .findFirst();
    }

    /**
     * Gets the current week number within the training plan.
     * @param tp training plan
     * @return current week number (1-based) or null if invalid
     */
    public Integer getCurrentWeekNb(TrainingPlan tp) {
        if (tp.getStartDate() == null || tp.getEndDate() == null) {
            return null;
        }
        if (tp.getStartDate().isAfter(tp.getEndDate())) {
            return null;
        }

        LocalDate currentDate = LocalDate.now();
        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(tp.getStartDate(), currentDate);
        return (int) weeksBetween + 1;
    }

    /**
     * Gets the current week number for a specific user.
     * @param accountId user identifier
     * @return current week number or null if no plan exists
     */
    public Integer getCurrentWeekNbForUser(UUID accountId) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        return tp.map(this::getCurrentWeekNb).orElse(null);
    }

    /**
     * Checks if the given week number is the last week of the training plan.
     * @param accountId user identifier
     * @param weekNb week number to check
     * @return true if it's the last week
     */
    public boolean checkIfItsLastWeek(UUID accountId, Integer weekNb) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        return tp.filter(trainingPlan -> weekNb.equals(trainingPlan.getWeeklyPlans().size())).isPresent();
    }

    /**
     * Checks if the current week is the last week of the training plan.
     * @param accountId user identifier
     * @return true if currently in the last week
     */
    public boolean checkIfLastWeek(UUID accountId) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        if (tp.isEmpty()) {
            return false;
        }
        Integer currentWeek = getCurrentWeekNb(tp.get());
        return currentWeek != null && currentWeek.equals(tp.get().getWeeklyPlans().size());
    }

    /**
     * Gets the week number for a specific date within the training plan.
     * @param tp training plan
     * @param date target date
     * @return week number (1-based) or null if date is outside plan period
     */
    public Integer getWeekNumberForDate(TrainingPlan tp, LocalDate date) {
        if (tp.getStartDate() == null || tp.getEndDate() == null) {
            return null;
        }
        if (tp.getStartDate().isAfter(tp.getEndDate())) {
            return null;
        }
        if (date.isBefore(tp.getStartDate()) || date.isAfter(tp.getEndDate())) {
            return null;
        }

        long weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(tp.getStartDate(), date);
        return (int) weeksBetween + 1;
    }

    /**
     * Gets the weekly plan for a specific date.
     * @param accountId user identifier
     * @param date target date
     * @return WeeklyPlan for the given date or null if not found
     */
    public WeeklyPlan getWeeklyPlanForDate(UUID accountId, LocalDate date) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        if (tp.isEmpty()) {
            return null;
        }
        Integer weekNumber = getWeekNumberForDate(tp.get(), date);
        if (weekNumber == null || weekNumber < 1 || weekNumber > tp.get().getWeeklyPlans().size()) {
            return null;
        }
        return tp.get().getWeeklyPlans().get(weekNumber - 1);
    }

    /**
     * Gets the weekly plan for a specific date using an existing training plan.
     * @param tp training plan
     * @param accountId user identifier
     * @param date target date
     * @return WeeklyPlan for the given date or null if not found
     */
    public WeeklyPlan getWeeklyPlanForDate(TrainingPlan tp, UUID accountId, LocalDate date) {
        Integer weekNumber = getWeekNumberForDate(tp, date);
        if (weekNumber == null || weekNumber < 1 || weekNumber > tp.getWeeklyPlans().size()) {
            return null;
        }
        return tp.getWeeklyPlans().get(weekNumber - 1);
    }

    /**
     * Gets the date range for upcoming workouts (current and next week).
     * @param accountId user identifier
     * @return list of OffsetDateTime representing the date range
     */
    public List<OffsetDateTime> getDatesForNextWorkouts(UUID accountId) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        if (tp.isEmpty()) {
            return List.of();
        }

        List<OffsetDateTime> dates = new ArrayList<>();
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

    /**
     * Gets the number of workouts per week for the current period.
     * @param accountId user identifier
     * @return number of workouts per week or null if not available
     */
    public Integer getNbWorkoutsPerWeek(UUID accountId) {
        Optional<TrainingPlan> tp = getMyTrainingPlan(accountId);
        if (tp.isEmpty()) {
            return null;
        }
        if (tp.get().getStartDate() == null || tp.get().getEndDate() == null) {
            return null;
        }
        if (tp.get().getStartDate().isAfter(tp.get().getEndDate())) {
            return null;
        }

        if(LocalDate.now().isBefore(tp.get().getStartDate())) {
            return tp.get().getWeeklyPlans().getFirst().getDailyPlans().size();
        } else if(LocalDate.now().isBefore(tp.get().getEndDate()) && LocalDate.now().isAfter(tp.get().getStartDate())) {
            int indexCurrentWeeklyPlan = getCurrentWeekNb(tp.get()) - 1;
            return tp.get().getWeeklyPlans().get(indexCurrentWeeklyPlan).getDailyPlans().size();
        } else {
            throw new IllegalStateException("We can't get the number of workouts.");
        }
    }
}