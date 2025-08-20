package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.openapi.annotations.links.Link;
import org.heigvd.dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.FitnessLevel;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.service.AccountService;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.service.WorkoutService;
import org.heigvd.training_generator.TrainingGeneratorV1;
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

    @Inject
    TrainingGeneratorV1 trainingGeneratorV1;

    @Test
    @Transactional
    public void printNextNWorkouts() {
        Goal g1 = goalService.getSpecificGoal(Sport.CYCLING, 180.0);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        LocalDate endDate = LocalDate.now().plusWeeks(16);

        Account account = new Account("guillaumetrueb@etik.com", "test1234", "Guillaume", "Trüeb", "079 999 99 99",
                LocalDate.of(1999, 9, 21), 77.0, 176.0, 205);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 75));

        accountService.create(account);

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(endDate, days, List.of(g1), true);
        TrainingPlan tp = trainingGeneratorV1.generateTrainingPlan(dto, account);
        tp = trainingGeneratorV1.generateTrainingWorkouts(tp);

        trainingPlanService.create(tp);

        List<Workout> workouts = workoutService.getCurrentWeekWorkouts(account.getId());
    }
}
