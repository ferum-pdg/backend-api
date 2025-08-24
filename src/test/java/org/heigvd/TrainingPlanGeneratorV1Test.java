package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.training_generator.generatorV1.TrainingPlanGeneratorV1;

@QuarkusTest
public class TrainingPlanGeneratorV1Test {

    @Inject
    TrainingPlanGeneratorV1 tpGen;

    void testCalculateNbOfWorkoutPerWeek() {

    }
}
