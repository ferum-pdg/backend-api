package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.service.GoalService;
import org.heigvd.service.TrainingPlanService;
import org.heigvd.training_generator.TrainingGeneratorV1;
import org.heigvd.training_generator.generator_V1.TrainingPlanGeneratorV1;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TrainingPlanGeneratorV1Test {

    @Inject
    TrainingPlanGeneratorV1 tpGen;

    @Inject
    TrainingGeneratorV1 tgV1;

    @Inject
    TrainingPlanService tpService;

    @Inject
    GoalService goalService;

    @Test
    void testDateCalculations() {

    }
}
