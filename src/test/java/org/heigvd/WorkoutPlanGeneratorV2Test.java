package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.FitnessLevel;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.entity.workout.details.WorkoutPlan;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_generator.generator_V1.TrainingPlanGeneratorV1;
import org.heigvd.training_generator.generator_V2.TrainingWorkoutsGeneratorV2;
import org.heigvd.training_generator.generator_V2.WorkoutPlanGeneratorV2;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@QuarkusTest
public class WorkoutPlanGeneratorV2Test {

    @Inject
    GoalService goalService;

    @Inject
    TrainingPlanService tpService;

    @Inject
    TrainingPlanGeneratorV1 tpGen;

    @Inject
    TrainingWorkoutsGeneratorV2 twGen;

    @Inject
    WorkoutPlanGeneratorV2 wpGen;

    private LocalDate computeEndDate(List<Goal> goals) {
        int totalWeeks = goals.stream().mapToInt(Goal::getNbOfWeek).sum();
        return LocalDate.now().plusWeeks(totalWeeks);
    }

    @Test
    void genereateWorkoutPlan() {
        List<WorkoutPlan> plans = wpGen.generateWorkout(
                Sport.RUNNING,
                WorkoutType.INTERVAL,
                50,
                0.3,
                TrainingPlanPhase.BASE
        );

        System.out.println(plans);
    }

    @Test
    void testTrivialTPGenerator() {
        Goal running = goalService.getSpecificGoal(Sport.RUNNING, 10.0);
        List<Goal> goals = List.of(running);

        LocalDate endDate = computeEndDate(goals);
        List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);

        Account account = new Account("runner@etik.com", "Test", "Runner",
                LocalDate.of(1990, 1, 1), 70.0, 175.0, 180);
        account.addFitnessLevel(new FitnessLevel(LocalDate.now(), 70));

        TrainingPlanRequestDto dto = new TrainingPlanRequestDto(endDate, days, goals, true);
        TrainingPlan tp = tpGen.generate(dto, account);

        List<Workout> workouts = twGen.generate(tp, LocalDate.now());

        System.out.println(workouts);
    }
}
