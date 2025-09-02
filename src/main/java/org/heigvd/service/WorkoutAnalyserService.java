package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.entity.workout.Workout;
import org.heigvd.workout_analyser.AnalyserFactory;
import org.heigvd.workout_analyser.interfaces.WorkoutAnalyser;

/**
 * Service for workout analysis operations.
 *
 * Provides workout analysis functionality using different analyser implementations
 * obtained through the factory pattern.
 */
@ApplicationScoped
public class WorkoutAnalyserService {

    @Inject
    AnalyserFactory analyserFactory;

    /**
     * Analyzes a workout using the configured workout analyser.
     *
     * @param workout the workout to analyze
     * @return the analyzed workout with updated metrics and insights
     */
    public Workout analyse(Workout workout) {
        WorkoutAnalyser analyser = analyserFactory.getWorkoutAnalyser();
        return analyser.analyse(workout);
    }
}