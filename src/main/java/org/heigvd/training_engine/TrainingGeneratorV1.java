package org.heigvd.training_engine;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.TrainingPlan;
import org.heigvd.entity.Account;
import org.heigvd.entity.Workout;

import java.util.List;

@ApplicationScoped
public class TrainingGeneratorV1 implements TrainingGenerator {

    public List<Workout> generateTraining(Account account, TrainingPlan trainingPlan) {

        return null;

    }
}
