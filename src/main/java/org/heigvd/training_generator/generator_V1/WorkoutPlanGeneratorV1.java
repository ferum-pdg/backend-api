package org.heigvd.training_generator.generator_V1;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.entity.workout.details.WorkoutPlan;
import org.heigvd.training_generator.interfaces.WorkoutPlanGenerator;

import java.util.List;

/**
 * Version 1 implementation of the workout plan generator.
 * This basic implementation currently provides a placeholder for
 * detailed workout plan generation functionality.
 *
 * The V1 implementation does not include detailed workout plan
 * generation capabilities. This functionality is available in V2
 * and later implementations.
 *
 * @version 1.0
 */
@ApplicationScoped
public class WorkoutPlanGeneratorV1 implements WorkoutPlanGenerator {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return "V1";
    }

    /**
     * Placeholder method for workout plan generation.
     * This implementation does not yet support detailed workout plan
     * generation and throws an UnsupportedOperationException.
     *
     * @param sport the sport for the workout
     * @param workoutType the type of workout to generate
     * @param fitnessLevel the athlete's fitness level (1-100)
     * @param progressionPercent the progression through the training plan (0.0-1.0)
     * @param phase the current training phase
     * @return not implemented - throws exception
     * @throws UnsupportedOperationException always thrown as this feature is not implemented
     */
    @Override
    public List<WorkoutPlan> generate(
            Sport sport,
            WorkoutType workoutType,
            int fitnessLevel,
            double progressionPercent,
            TrainingPlanPhase phase
    ) {
        throw new UnsupportedOperationException("Not implemented.");
    }
}