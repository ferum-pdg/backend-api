package org.heigvd.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.heigvd.entity.workout.Workout;
import org.heigvd.workout_analyser.AnalyserFactory;
import org.heigvd.workout_analyser.interfaces.WorkoutAnalyser;

@ApplicationScoped
public class WorkoutAnalyserService {

    @Inject
    AnalyserFactory analyserFactory;

    public Workout analyse(Workout workout) {
        WorkoutAnalyser analyser = analyserFactory.getWorkoutAnalyser();
        return analyser.analyse(workout);
    }
}
