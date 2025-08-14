package org.heigvd.training_engine;

import org.heigvd.entity.TrainingPlan.TrainingPlan;

public interface TrainingGenerator {

    public TrainingPlan generateTrainingWorkouts(TrainingPlan trainingPlan);
}
