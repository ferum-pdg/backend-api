package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.dto.training_plan_dto.TrainingPlanRequestDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.training_plan.TrainingPlan;
import org.heigvd.entity.training_plan.TrainingPlanPhase;
import org.heigvd.entity.workout.Workout;
import org.heigvd.entity.workout.WorkoutType;
import org.heigvd.entity.workout.details.WorkoutPlan;
import org.heigvd.training_generator.GeneratorFactory;
import org.heigvd.training_generator.interfaces.TrainingPlanGenerator;
import org.heigvd.training_generator.interfaces.TrainingWorkoutGenerator;
import org.heigvd.training_generator.interfaces.WorkoutPlanGenerator;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class TrainingGeneratorService {

    @Inject
    GeneratorFactory generatorFactory;

    public TrainingPlan generate(TrainingPlanRequestDto request, Account account) {
        TrainingPlanGenerator generator = generatorFactory.getTrainingPlanGenerator();
        return generator.generate(request, account);
    }

    public List<Workout> generate(TrainingPlan trainingPlan, LocalDate date) {
        TrainingWorkoutGenerator generator = generatorFactory.getTrainingWorkoutGenerator();
        return generator.generate(trainingPlan, date);
    }

    public List<WorkoutPlan> generate(
            Sport sport,
            WorkoutType workoutType,
            int fitnessLevel,
            double progressionPercent,
            TrainingPlanPhase phase) {
        WorkoutPlanGenerator generator = generatorFactory.getWorkoutPlanGenerator();
        return generator.generate(sport, workoutType, fitnessLevel, progressionPercent, phase);
    }

    // Méthodes utilitaires pour obtenir les versions actuellement utilisées
    public String getCurrentTrainingPlanGeneratorVersion() {
        return generatorFactory.getTrainingPlanGenerator().getVersion();
    }

    public String getCurrentWorkoutGeneratorVersion() {
        return generatorFactory.getTrainingWorkoutGenerator().getVersion();
    }

    public String getCurrentWorkoutPlanGeneratorVersion() {
        return generatorFactory.getWorkoutPlanGenerator().getVersion();
    }
}