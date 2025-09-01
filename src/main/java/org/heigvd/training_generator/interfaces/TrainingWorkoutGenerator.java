package org.heigvd.training_generator.interfaces;

import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.workout.Workout;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface for workout generation implementations.
 * This interface defines the contract for generating individual workouts
 * from training plans, including both on-demand generation and synchronization
 * of workout schedules.
 *
 * Implementations should provide:
 * - Intelligent workout type selection
 * - Dynamic duration calculation
 * - Detailed workout plan generation
 * - Schedule synchronization capabilities
 *
 * @version 1.0
 */
public interface TrainingWorkoutGenerator {

    /**
     * Generates workouts for a specific date based on the training plan.
     * This method creates workout instances with appropriate types, durations,
     * and detailed plans based on the training plan's schedule and progression.
     *
     * Typically generates workouts for the current week and potentially
     * the following week to ensure continuity.
     *
     * @param trainingPlan the overall training plan
     * @param actualDate the date for which to generate workouts
     * @return a list of generated workouts
     * @throws IllegalArgumentException if no plan exists for the given date
     */
    List<Workout> generate(TrainingPlan trainingPlan, LocalDate actualDate);

    /**
     * Synchronizes the workout schedule by generating any missing workouts.
     * This method ensures continuity in the workout schedule by identifying
     * gaps and generating the necessary workout instances.
     *
     * Used to maintain schedule integrity and ensure athletes always have
     * upcoming workouts planned.
     *
     * @param trainingPlan the training plan to synchronize
     * @param actualDate the current date for synchronization reference
     * @return a list of newly generated workouts
     * @throws IllegalArgumentException if the training plan is null
     */
    List<Workout> sync(TrainingPlan trainingPlan, LocalDate actualDate);

    /**
     * Returns the version identifier for this implementation.
     * This allows the system to track which implementation was used
     * for generating specific workouts.
     *
     * @return the version identifier (e.g., "V1", "V2")
     */
    String getVersion();
}