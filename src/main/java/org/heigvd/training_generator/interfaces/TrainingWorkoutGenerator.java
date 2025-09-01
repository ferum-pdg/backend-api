package org.heigvd.training_generator.interfaces;

import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.workout.Workout;
import java.time.LocalDate;
import java.util.List;

public interface TrainingWorkoutGenerator {
    List<Workout> generate(TrainingPlan trainingPlan, LocalDate actualDate);
    String getVersion();
}