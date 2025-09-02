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

/**
 * Service for training plan and workout generation.
 *
 * Provides methods to generate training plans, workouts, and workout plans
 * using different generator implementations.
 */
@ApplicationScoped
public class TrainingGeneratorService {

    @Inject
    GeneratorFactory generatorFactory;

    /**
     * Generates a training plan based on the request and user account.
     *
     * @param request training plan generation request
     * @param account user account
     * @return generated training plan
     */
    public TrainingPlan generate(TrainingPlanRequestDto request, Account account) {
        TrainingPlanGenerator generator = generatorFactory.getTrainingPlanGenerator();
        return generator.generate(request, account);
    }

    /**
     * Generates workouts for a specific date within a training plan.
     *
     * @param trainingPlan the training plan
     * @param date target date for workout generation
     * @return list of generated workouts
     */
    public List<Workout> generate(TrainingPlan trainingPlan, LocalDate date) {
        TrainingWorkoutGenerator generator = generatorFactory.getTrainingWorkoutGenerator();
        return generator.generate(trainingPlan, date);
    }

    /**
     * Generates workout plans based on sport, type, fitness level and phase.
     *
     * @param sport target sport
     * @param workoutType type of workout
     * @param fitnessLevel user's fitness level
     * @param progressionPercent progression percentage
     * @param phase training plan phase
     * @return list of generated workout plans
     */
    public List<WorkoutPlan> generate(
            Sport sport,
            WorkoutType workoutType,
            int fitnessLevel,
            double progressionPercent,
            TrainingPlanPhase phase) {
        WorkoutPlanGenerator generator = generatorFactory.getWorkoutPlanGenerator();
        return generator.generate(sport, workoutType, fitnessLevel, progressionPercent, phase);
    }

    /**
     * Synchronizes workouts in a training plan for the current date.
     *
     * @param trainingPlan the training plan to sync
     * @param today current date
     * @return list of synchronized workouts
     */
    public List<Workout> sync(TrainingPlan trainingPlan, LocalDate today) {
        TrainingWorkoutGenerator generator = generatorFactory.getTrainingWorkoutGenerator();
        return generator.sync(trainingPlan, today);
    }

    /**
     * Gets the version of the currently used training plan generator.
     *
     * @return version string of the training plan generator
     */
    public String getCurrentTrainingPlanGeneratorVersion() {
        return generatorFactory.getTrainingPlanGenerator().getVersion();
    }

    /**
     * Gets the version of the currently used workout generator.
     *
     * @return version string of the workout generator
     */
    public String getCurrentWorkoutGeneratorVersion() {
        return generatorFactory.getTrainingWorkoutGenerator().getVersion();
    }

    /**
     * Gets the version of the currently used workout plan generator.
     *
     * @return version string of the workout plan generator
     */
    public String getCurrentWorkoutPlanGeneratorVersion() {
        return generatorFactory.getWorkoutPlanGenerator().getVersion();
    }
}