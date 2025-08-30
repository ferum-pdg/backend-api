package org.heigvd.training_generator.generator_V2;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.training_generator.interfaces.TrainingPlanGenerator;
import org.heigvd.training_generator.generator_V1.TrainingPlanGeneratorV1;

@ApplicationScoped
public class TrainingPlanGeneratorV2 implements TrainingPlanGenerator {

    @Inject
    TrainingPlanGeneratorV1 tpGenV1;

    @Override
    public String getVersion() {
        return "V2";
    }

    @Override
    public TrainingPlan generate(TrainingPlanRequestDto tpDto, Account account) {
        return tpGenV1.generate(tpDto, account);
    }
}
