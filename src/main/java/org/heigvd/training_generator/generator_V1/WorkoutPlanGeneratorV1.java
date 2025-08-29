package org.heigvd.training_generator.generator_V1;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.entity.workout.details.WorkoutPlan;
import org.heigvd.training_generator.interfaces.WorkoutPlanGenerator;

import java.util.List;

@ApplicationScoped
public class WorkoutPlanGeneratorV1 implements WorkoutPlanGenerator {

    @Override
    public String getVersion() {
        return "V1";
    }

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