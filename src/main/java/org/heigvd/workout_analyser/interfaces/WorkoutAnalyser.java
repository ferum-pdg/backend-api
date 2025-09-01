package org.heigvd.workout_analyser.interfaces;

import org.heigvd.entity.workout.Workout;

/**
 * Interface for workout analysis implementations.
 * This interface defines the contract for analyzing completed workouts
 * and providing performance feedback and metrics.
 *
 * Implementations should provide:
 * - Comprehensive performance evaluation
 * - Multi-dimensional scoring systems
 * - AI-powered feedback generation
 * - Fitness progression tracking
 *
 * @version 1.0
 */
public interface WorkoutAnalyser {

    /**
     * Analyzes a completed workout and generates comprehensive performance metrics.
     * This method should evaluate workout quality across multiple dimensions,
     * generate appropriate feedback, and update fitness progression tracking.
     *
     * Only completed workouts should be analyzed - workouts in other states
     * should be returned unchanged.
     *
     * Analysis should include:
     * - Performance grading based on objective metrics
     * - Comparison against planned targets
     * - Data quality assessment
     * - AI-generated feedback and recommendations
     * - Fitness level progression updates
     *
     * @param workout the workout to analyze (should be completed)
     * @return the workout with updated analysis results and feedback
     */
    Workout analyse(Workout workout);
}