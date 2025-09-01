package org.heigvd.training_generator.generator_V2;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.training_generator.interfaces.TrainingPlanGenerator;
import org.heigvd.training_generator.generator_V1.TrainingPlanGeneratorV1;

/**
 * Version 2 implementation of the training plan generator.
 * This implementation currently delegates to the V1 implementation
 * while providing a foundation for future enhancements.
 *
 * Future V2 enhancements may include:
 * - Advanced periodization algorithms
 * - Machine learning-based optimization
 * - Enhanced constraint handling
 * - Improved goal integration
 *
 * @version 2.0
 */
@ApplicationScoped
public class TrainingPlanGeneratorV2 implements TrainingPlanGenerator {

    @Inject
    TrainingPlanGeneratorV1 tpGenV1;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return "V2";
    }

    /**
     * Generates a training plan by delegating to the V1 implementation.
     * This temporary approach maintains compatibility while allowing
     * for incremental V2 feature development.
     *
     * @param tpDto the training plan request
     * @param account the user account
     * @return a complete training plan
     */
    @Override
    public TrainingPlan generate(TrainingPlanRequestDto tpDto, Account account) {
        return tpGenV1.generate(tpDto, account);
    }
}