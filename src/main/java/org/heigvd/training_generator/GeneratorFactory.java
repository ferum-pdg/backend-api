package org.heigvd.training_generator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.training_generator.generator_V1.TrainingPlanGeneratorV1;
import org.heigvd.training_generator.generator_V1.WorkoutGeneratorV1;
import org.heigvd.training_generator.generator_V1.WorkoutPlanGeneratorV1;
import org.heigvd.training_generator.generator_V2.TrainingPlanGeneratorV2;
import org.heigvd.training_generator.generator_V2.WorkoutGeneratorV2;
import org.heigvd.training_generator.generator_V2.WorkoutPlanGeneratorV2;
import org.heigvd.training_generator.interfaces.TrainingPlanGenerator;
import org.heigvd.training_generator.interfaces.TrainingWorkoutGenerator;
import org.heigvd.training_generator.interfaces.WorkoutPlanGenerator;

@ApplicationScoped
public class GeneratorFactory {

    // V1 --------------------------------------------------------------------------------------------------------------
    @Inject
    TrainingPlanGeneratorV1 trainingPlanGeneratorV1;
    @Inject
    WorkoutGeneratorV1 workoutGeneratorV1;
    @Inject
    WorkoutPlanGeneratorV1 workoutPlanGeneratorV1;

    // V2 --------------------------------------------------------------------------------------------------------------
    @Inject
    TrainingPlanGeneratorV2 trainingPlanGeneratorV2;
    @Inject
    WorkoutGeneratorV2 workoutGeneratorV2;
    @Inject
    WorkoutPlanGeneratorV2 workoutPlanGeneratorV2;


    // Configuration ---------------------------------------------------------------------------------------------------
    @Inject
    GeneratorConfiguration config;

    public TrainingPlanGenerator getTrainingPlanGenerator() {
        return switch (config.getGlobalVersion()) {
            case "V1" -> trainingPlanGeneratorV1;
            case "V2" -> trainingPlanGeneratorV2;
            default -> throw new IllegalStateException("Unknown version: " + config.getGlobalVersion());
        };
    }

    public TrainingWorkoutGenerator getTrainingWorkoutGenerator() {
        return switch (config.getGlobalVersion()) {
            case "V1" -> workoutGeneratorV1;
            case "V2" -> workoutGeneratorV2;
            default -> throw new IllegalStateException("Unknown version: " + config.getGlobalVersion());
        };
    }

    public WorkoutPlanGenerator getWorkoutPlanGenerator() {
        return switch (config.getGlobalVersion()) {
            case "V1" -> workoutPlanGeneratorV1;
            case "V2" -> workoutPlanGeneratorV2;
            default -> throw new IllegalStateException("Unknown version: " + config.getGlobalVersion());
        };
    }
}
