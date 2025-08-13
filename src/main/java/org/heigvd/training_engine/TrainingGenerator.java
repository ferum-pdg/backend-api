package org.heigvd.training_engine;

import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.Workout.Workout;

import java.util.List;

public interface TrainingGenerator {

    public void generateTrainingWorkouts(TrainingPlan trainingPlan);
}
