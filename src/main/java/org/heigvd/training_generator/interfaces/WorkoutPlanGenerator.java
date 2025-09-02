package org.heigvd.training_generator.interfaces;

import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.entity.workout.details.WorkoutPlan;

import java.util.List;

/**
 * Interface for detailed workout plan generation implementations.
 * This interface defines the contract for creating structured workout plans
 * with specific segments, intensities, and durations.
 *
 * Implementations should provide:
 * - Sport-specific workout structures
 * - Adaptive parameter calculation
 * - Phase-aware progression
 * - Intensity zone management
 *
 * @version 1.0
 */
public interface WorkoutPlanGenerator {

    /**
     * Generates a detailed workout plan with structured segments and intensities.
     * This method creates a comprehensive workout structure including warm-up,
     * main work, and cool-down phases with appropriate intensity distributions.
     *
     * The generated plan should adapt to the athlete's current fitness level,
     * training phase, and progression through the overall training plan.
     *
     * @param sport the sport for the workout (affects structure and durations)
     * @param workoutType the type of workout to generate (determines intensity focus)
     * @param fitnessLevel the athlete's current fitness level (1-100)
     * @param progressionPercent the progression through the training plan (0.0-1.0)
     * @param phase the current training phase (affects emphasis and parameters)
     * @return a list of workout plan blocks with detailed segments
     * @throws IllegalArgumentException if parameters are invalid or unsupported
     */
    List<WorkoutPlan> generate(
            Sport sport,
            WorkoutType workoutType,
            int fitnessLevel,
            double progressionPercent,
            TrainingPlanPhase phase
    );

    /**
     * Returns the version identifier for this implementation.
     * This allows the system to track which implementation was used
     * for generating specific workout plans.
     *
     * @return the version identifier (e.g., "V1", "V2")
     */
    String getVersion();
}