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

/**
 * Factory class responsible for providing the correct implementation
 * of training generation components based on the configured version.
 *
 * This factory acts as a central point for version management and ensures
 * that all components use compatible implementations.
 *
 * @version 2.0
 */
@ApplicationScoped
public class GeneratorFactory {

    // V1 Implementation Injections

    @Inject
    TrainingPlanGeneratorV1 trainingPlanGeneratorV1;

    @Inject
    WorkoutGeneratorV1 workoutGeneratorV1;

    @Inject
    WorkoutPlanGeneratorV1 workoutPlanGeneratorV1;

    // V2 Implementation Injections

    @Inject
    TrainingPlanGeneratorV2 trainingPlanGeneratorV2;

    @Inject
    WorkoutGeneratorV2 workoutGeneratorV2;

    @Inject
    WorkoutPlanGeneratorV2 workoutPlanGeneratorV2;

    // Configuration

    @Inject
    GeneratorConfiguration config;

    /**
     * Retrieves the appropriate training plan generator implementation
     * based on the current global configuration version.
     *
     * @return the training plan generator implementation
     * @throws IllegalStateException if the configured version is unknown
     */
    public TrainingPlanGenerator getTrainingPlanGenerator() {
        return switch (config.getGlobalVersion()) {
            case "V1" -> trainingPlanGeneratorV1;
            case "V2" -> trainingPlanGeneratorV2;
            default -> throw new IllegalStateException("Unknown version: " + config.getGlobalVersion());
        };
    }

    /**
     * Retrieves the appropriate training workout generator implementation
     * based on the current global configuration version.
     *
     * @return the training workout generator implementation
     * @throws IllegalStateException if the configured version is unknown
     */
    public TrainingWorkoutGenerator getTrainingWorkoutGenerator() {
        return switch (config.getGlobalVersion()) {
            case "V1" -> workoutGeneratorV1;
            case "V2" -> workoutGeneratorV2;
            default -> throw new IllegalStateException("Unknown version: " + config.getGlobalVersion());
        };
    }

    /**
     * Retrieves the appropriate workout plan generator implementation
     * based on the current global configuration version.
     *
     * @return the workout plan generator implementation
     * @throws IllegalStateException if the configured version is unknown
     */
    public WorkoutPlanGenerator getWorkoutPlanGenerator() {
        return switch (config.getGlobalVersion()) {
            case "V1" -> workoutPlanGeneratorV1;
            case "V2" -> workoutPlanGeneratorV2;
            default -> throw new IllegalStateException("Unknown version: " + config.getGlobalVersion());
        };
    }
}