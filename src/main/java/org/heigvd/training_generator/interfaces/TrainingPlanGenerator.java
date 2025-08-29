package org.heigvd.training_generator.interfaces;

import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;

public interface TrainingPlanGenerator {
    TrainingPlan generate(TrainingPlanRequestDto request, Account account);
    String getVersion();
}
