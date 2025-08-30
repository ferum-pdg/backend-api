package org.heigvd.workout_analyser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.workout_analyser.interfaces.WorkoutAnalyser;

@ApplicationScoped
public class AnalyserFactory {

    // V1 --------------------------------------------------------------------------------------------------------------
    @Inject
    WorkoutAnalyser workoutAnalyserV1;


    // Configuration ---------------------------------------------------------------------------------------------------
    @Inject
    AnalyserConfiguration config;

    public WorkoutAnalyser getWorkoutAnalyser() {
        return switch (config.getGlobalVersion()) {
            case "V1" -> workoutAnalyserV1;
            default -> throw new IllegalStateException("Unknown version: " + config.getGlobalVersion());
        };
    }
}
