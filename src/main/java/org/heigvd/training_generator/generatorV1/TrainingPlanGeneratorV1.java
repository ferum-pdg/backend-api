package org.heigvd.training_generator.generatorV1;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.service.GoalService;
import org.heigvd.training_generator.tools.SportNbTraining;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TrainingPlanGeneratorV1 {

    @Inject
    GoalService goalService;

    // GENERATORS ------------------------------------------------------------------------------------------------------

    public TrainingPlan generate(TrainingPlanRequestDto tpDto, Account account) {

        List<Goal> goals = goalService.getGoalsByIds(tpDto.getGoalIds());

        if(goals.isEmpty()) {
            throw new IllegalArgumentException("No available goals for the training plan.");
        }

        List<DayOfWeek> availableDays = tpDto.getDaysOfWeek().stream()
                .map(DayOfWeek::valueOf)
                .toList();



        return null;
    }

    private void generateWeeklyPlan() {}

    private void generateDailyPlan() {}

    // CALCULATORS -----------------------------------------------------------------------------------------------------

    public int calculateNbWeeksOfTraining(List<Goal> goals) {
        return goals.stream().mapToInt(Goal::getNbOfWeek).max().orElse(0);
    }

    public int calculateNbOfWorkoutsPerWeek(List<Goal> goals, int meanFitnessLevel) {
        return 0;
    }

    public int calculateNbOfWorkoutsPerSportPerWeek(List<Goal> goals, Account account) {
        return 0;
    }

    public void calculateWeekSportRepartition(List<Goal> goals, int nbOfWorkoutPerWeek) {

    }

    // FORMULAS --------------------------------------------------------------------------------------------------------

    public double fitnessLevelPonderation(int accountMeanFitnessLevel) {
        return 0.00625 * accountMeanFitnessLevel + 0.5;
    }

}
