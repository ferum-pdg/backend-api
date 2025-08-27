package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.FitnessLevel;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.workout.Workout;
import org.heigvd.service.AccountService;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.service.WorkoutService;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@QuarkusTest
public class WorkoutTest {

    @Inject
    GoalService goalService;

    @Inject
    WorkoutService workoutService;

    @Inject
    AccountService accountService;

    @Inject
    TrainingPlanService trainingPlanService;

}
