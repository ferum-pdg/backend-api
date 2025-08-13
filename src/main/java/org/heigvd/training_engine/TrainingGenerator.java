package org.heigvd.training_engine;

import org.heigvd.entity.TrainingPlan.TrainingPlan;

public interface TrainingGenerator {

    public void generateTrainingWorkouts(TrainingPlan trainingPlan);
}
