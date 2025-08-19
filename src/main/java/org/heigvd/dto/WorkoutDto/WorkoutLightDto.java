package org.heigvd.dto.WorkoutDto;

import org.heigvd.entity.Sport;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.entity.Workout.WorkoutType;

import java.time.OffsetDateTime;
import java.util.UUID;

public class WorkoutLightDto {

    private UUID id;
    private Sport sport;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private Double distance;
    private Double caloriesKcal;
    private String source;
    private WorkoutType workoutType;

    // CONSTRUCTORS --------------------------------

    public WorkoutLightDto() {}

    public WorkoutLightDto(UUID id, Sport sport, OffsetDateTime start, OffsetDateTime end, Double distance, Double caloriesKcal, String source, WorkoutType workoutType) {
        this.id = id;
        this.sport = sport;
        this.start = start;
        this.end = end;
        this.distance = distance;
        this.caloriesKcal = caloriesKcal;
        this.source = source;
        this.workoutType = workoutType;
    }

    public WorkoutLightDto(Workout workout) {
        this.id = workout.getId();
        this.sport = workout.getSport();
        this.start = workout.getStartTime();
        this.end = workout.getEndTime();
        this.distance = workout.getDistanceMeters();
        this.caloriesKcal = workout.getCaloriesKcal();
        this.source = workout.getSource();
        this.workoutType = workout.getWorkoutType();
    }

    // METHODS --------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public OffsetDateTime getStart() { return start; }
    public void setStart(OffsetDateTime start) { this.start = start; }

    public OffsetDateTime getEnd() { return end; }
    public void setEnd(OffsetDateTime end) { this.end = end; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Double getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(Double caloriesKcal) { this.caloriesKcal = caloriesKcal; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public WorkoutType getWorkoutType() { return workoutType; }
    public void setWorkoutType(WorkoutType workoutType) { this.workoutType = workoutType; }
}
