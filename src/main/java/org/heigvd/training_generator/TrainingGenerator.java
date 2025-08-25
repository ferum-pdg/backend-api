package org.heigvd.training_generator;

import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;

public interface TrainingGenerator {

    public TrainingPlan generateTrainingWorkouts(TrainingPlan trainingPlan);

    public TrainingPlan generateTrainingPlan(TrainingPlanRequestDto trainingPlanRequestDto, Account account);
}
