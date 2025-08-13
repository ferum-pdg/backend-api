package org.heigvd.entity.TrainingPlan;

import jakarta.persistence.*;
import org.heigvd.entity.Sport;
import org.heigvd.entity.Workout.WorkoutType;

import java.time.DayOfWeek;
import java.util.UUID;

@Entity
public class DailyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(name = "workout_type")
    private WorkoutType workoutType;

    // CONSTRUCTORS ---------------------------------------------

    public DailyPlan() { }

    public DailyPlan(DayOfWeek dayOfWeek, Sport sport, WorkoutType workoutType) {
        this.dayOfWeek = dayOfWeek;
        this.sport = sport;
    }

    // METHODS ---------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public WorkoutType getWorkoutType() { return workoutType; }
    public void setWorkoutType(WorkoutType workoutType) { this.workoutType = workoutType; }

    @Override
    public String toString() {
        return "\n { \n" +
                "  dayOfWeek=" + dayOfWeek + "\n" +
                "  sport=" + sport + "\n" +
                "  workoutType=" + workoutType + "\n" +
                " }";
    }
}
