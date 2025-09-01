package org.heigvd.workout_analyser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.workout_analyser.interfaces.WorkoutAnalyser;

/**
 * Factory class responsible for providing the correct implementation
 * of workout analysis components based on the configured version.
 *
 * This factory acts as a central point for version management and ensures
 * that all analysis components use compatible implementations.
 *
 * Currently supports V1 implementation with extensibility for future versions.
 *
 * @version 1.0
 */
@ApplicationScoped
public class AnalyserFactory {

    /**
     * V1 workout analyzer implementation injection
     */
    @Inject
    WorkoutAnalyser workoutAnalyserV1;

    /**
     * Configuration management injection
     */
    @Inject
    AnalyserConfiguration config;

    /**
     * Retrieves the appropriate workout analyzer implementation
     * based on the current global configuration version.
     *
     * @return the workout analyzer implementation
     * @throws IllegalStateException if the configured version is unknown
     */
    public WorkoutAnalyser getWorkoutAnalyser() {
        return switch (config.getGlobalVersion()) {
            case "V1" -> workoutAnalyserV1;
            default -> throw new IllegalStateException("Unknown version: " + config.getGlobalVersion());
        };
    }
}