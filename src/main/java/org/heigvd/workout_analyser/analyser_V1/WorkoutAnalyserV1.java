package org.heigvd.workout_analyser.analyser_V1;

import jakarta.enterprise.context.ApplicationScoped;
import org.heigvd.entity.workout.Workout;
import org.heigvd.workout_analyser.interfaces.WorkoutAnalyser;

@ApplicationScoped
public class WorkoutAnalyserV1 implements WorkoutAnalyser {

    public Workout analyse(Workout workout) {
        // Basic analysis: just return the workout as is
        return workout;
    }

    private int gradeWorkout(Workout workout) {

        return 0;
    }
}
