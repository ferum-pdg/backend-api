package org.heigvd.training_engine;

import org.heigvd.entity.Account;
import org.heigvd.entity.TrainingPlan;
import org.heigvd.entity.Workout;

import java.util.List;

public interface TrainingGenerator {

    public List<Workout> generateTrainingWorkouts(Account account, TrainingPlan trainingPlan);
}
