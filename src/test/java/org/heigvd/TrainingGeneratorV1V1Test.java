package org.heigvd;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.heigvd.training_engine.TrainingGeneratorV1;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TrainingGeneratorV1V1Test {

    @Inject
    TrainingGeneratorV1 trainingGeneratorV1;

    @Test
    void generateTraining() {
    }
}
