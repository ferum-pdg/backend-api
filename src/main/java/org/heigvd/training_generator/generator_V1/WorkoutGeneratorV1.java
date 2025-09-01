package org.heigvd.training_generator.generator_V1;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.DailyPlan;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.training_plan.WeeklyPlan;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutStatus;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.service.WorkoutService;
import org.heigvd.training_generator.interfaces.TrainingWorkoutGenerator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Version 1 implementation of the workout generator.
 * This basic implementation provides fundamental workout generation capabilities
 * with simple scheduling and consistent workout creation.
 *
 * Key features:
 * - Basic workout scheduling for current and next week
 * - Fixed workout timing (6 PM start, 1 hour duration)
 * - Simple synchronization with last generated workout tracking
 * - Consistent workout type assignment (EF - Endurance Fondamentale)
 *
 * This implementation serves as a foundation for more advanced generators
 * and provides reliable basic functionality.
 *
 * @version 1.0
 */
@ApplicationScoped
public class WorkoutGeneratorV1 implements TrainingWorkoutGenerator {

    @Inject
    TrainingPlanService trainingPlanService;

    @Inject
    WorkoutService workoutService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return "V1";
    }

    /**
     * Generates workouts for the current week and optionally the following week.
     * This basic implementation creates simple workouts with fixed parameters
     * and consistent scheduling.
     *
     * @param trainingPlan the overall training plan
     * @param actualDate the current date for workout generation
     * @return a list of generated workouts
     * @throws IllegalArgumentException if no weekly plan is found for the given date
     */
    @Override
    public List<Workout> generate(TrainingPlan trainingPlan, LocalDate actualDate) {

        Account account = trainingPlan.getAccount();

        Integer currentWeekNumber = trainingPlanService.getWeekNumberForDate(trainingPlan, actualDate);
        WeeklyPlan currentWeek = trainingPlanService.getWeeklyPlanForDate(account.getId(), actualDate);

        if (currentWeek == null) {
            throw new IllegalArgumentException("No weekly plan found for the given date.");
        }

        LocalDate monday = actualDate.minusDays(actualDate.getDayOfWeek().getValue() - 1);

        List<Workout> workouts = generateWorkoutForWeek(
                trainingPlan,
                currentWeek,
                monday,
                account
        );

        if (currentWeekNumber != null && currentWeekNumber < trainingPlan.getWeeklyPlans().size()) {
            workouts.addAll(
                    generateWorkoutForWeek(
                            trainingPlan,
                            trainingPlan.getWeeklyPlans().get(currentWeekNumber + 1),
                            monday.plusWeeks(1),
                            account
                    )
            );
        }

        return workouts;
    }

    /**
     * Synchronizes workouts by generating missing ones up to the following week.
     * This method ensures continuity in workout scheduling by identifying
     * and filling gaps in the workout calendar.
     *
     * @param trainingPlan the training plan to synchronize
     * @param today the current date for synchronization reference
     * @return a list of newly generated workouts
     * @throws IllegalArgumentException if the training plan is null
     */
    @Override
    public List<Workout> sync(TrainingPlan trainingPlan, LocalDate today) {

        if (trainingPlan == null) {
            throw new IllegalArgumentException("Training plan cannot be null");
        }

        Account account = trainingPlan.getAccount();
        List<Workout> newWorkouts = new ArrayList<>();

        LocalDate lastGeneratedWorkoutDate = workoutService.getLastGeneratedWorkoutDate(account);
        LocalDate endDate = today.plusWeeks(1).with(java.time.DayOfWeek.SUNDAY);

        if (lastGeneratedWorkoutDate != null && !lastGeneratedWorkoutDate.isBefore(endDate)) {
            return newWorkouts;
        }

        int currentWeekNumber = trainingPlanService.getWeekNumberForDate(trainingPlan, today);

        newWorkouts.addAll(
                generateWorkoutForWeek(
                        trainingPlan,
                        trainingPlan.getWeeklyPlans().get(currentWeekNumber),
                        today.minusDays(today.getDayOfWeek().getValue() - 1),
                        account
                )
        );

        if (currentWeekNumber < trainingPlan.getWeeklyPlans().size()) {
            newWorkouts.addAll(
                    generateWorkoutForWeek(
                            trainingPlan,
                            trainingPlan.getWeeklyPlans().get(currentWeekNumber + 1),
                            today.plusWeeks(1).minusDays(today.plusWeeks(1).getDayOfWeek().getValue() - 1),
                            account
                    )
            );
        }

        return newWorkouts;
    }

    /**
     * Generates workouts for a complete week based on the weekly plan.
     * This method creates individual workout instances for each daily plan
     * with consistent parameters and scheduling.
     *
     * All workouts are scheduled for 6 PM with a 1-hour duration and
     * set to EF (Endurance Fondamentale) workout type.
     *
     * @param trainingPlan the overall training plan
     * @param plan the weekly plan containing daily schedules
     * @param monday the Monday of the target week
     * @param account the user account
     * @return a list of workouts for the week
     */
    private List<Workout> generateWorkoutForWeek(TrainingPlan trainingPlan, WeeklyPlan plan,
                                                 LocalDate monday, Account account) {

        List<DailyPlan> dailyPlans = plan.getDailyPlans();
        List<Workout> workouts = new ArrayList<>();

        for (DailyPlan dp : dailyPlans) {
            OffsetDateTime startTime = monday
                    .plusDays(dp.getDayOfWeek().getValue() - 1)
                    .atTime(18, 0)
                    .atOffset(OffsetDateTime.now().getOffset());

            workouts.add(
                    new Workout(
                            account,
                            dp.getSport(),
                            startTime,
                            startTime.plusHours(1),
                            "Training Workout Generator V1",
                            WorkoutStatus.PLANNED,
                            WorkoutType.EF,
                            trainingPlan
                    )
            );
        }

        return workouts;
    }
}