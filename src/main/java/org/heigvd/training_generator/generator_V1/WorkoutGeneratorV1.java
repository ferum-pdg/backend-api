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

@ApplicationScoped
public class WorkoutGeneratorV1 implements TrainingWorkoutGenerator {

    @Inject
    TrainingPlanService trainingPlanService;

    @Inject
    WorkoutService workoutService;

    @Override
    public String getVersion() {
        return "V1";
    }

    @Override
    public List<Workout> generate(TrainingPlan trainingPlan, LocalDate actualDate) {
        Account account = trainingPlan.getAccount();

        Integer currentWeekNumber = trainingPlanService.getWeekNumberForDate(trainingPlan, actualDate);
        WeeklyPlan currentWeek = trainingPlanService.getWeeklyPlanForDate(account.getId(), actualDate);

        if(currentWeek == null) {
            throw new IllegalArgumentException("No weekly plan found for the given date.");
        }

        // Get the monday of the current week
        LocalDate monday = actualDate.minusDays(actualDate.getDayOfWeek().getValue() - 1);

        List<Workout> workouts = generateWorkoutForWeek(
                trainingPlan,
                currentWeek,
                monday,
                account
        );

        if(currentWeekNumber != null && currentWeekNumber < trainingPlan.getWeeklyPlans().size()) {
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

    private List<Workout> generateWorkoutForWeek(TrainingPlan trainingPlan, WeeklyPlan plan,
                                                 LocalDate monday, Account account) {
        List<DailyPlan> dailyPlans = plan.getDailyPlans();
        List<Workout> workouts = new ArrayList<>();

        for(DailyPlan dp : dailyPlans) {
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
                            "Training Workout Genreator V1",
                            WorkoutStatus.PLANNED,
                            WorkoutType.EF,
                            trainingPlan
                    )
            );
        }

        return workouts;
    }
}
