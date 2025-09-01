package org.heigvd.training_generator.interfaces;

import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.entity.workout.details.WorkoutPlan;

import java.util.List;

public interface WorkoutPlanGenerator {
    List<WorkoutPlan> generate(
            Sport sport,
            WorkoutType workoutType,
            int fitnessLevel,
            double progressionPercent,
            TrainingPlanPhase phase
    );
    String getVersion();
}
