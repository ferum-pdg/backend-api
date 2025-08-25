package org.heigvd.dto.WorkoutDto;

import org.heigvd.entity.Sport;
import org.heigvd.entity.Workout.Workout;
import org.heigvd.entity.Workout.WorkoutStatus;
import org.heigvd.entity.Workout.WorkoutType;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.UUID;

public class WorkoutLightDto {

    private UUID id;
    private Sport sport;
    private WorkoutType type;
    private WorkoutStatus status;
    private DayOfWeek day;
    private Integer duration;
    private Integer week;
    private String source;


    // CONSTRUCTORS --------------------------------

    public WorkoutLightDto() {}

    public WorkoutLightDto(Workout workout) {
        this.id = workout.getId();
        this.sport = workout.getSport();
        this.type = workout.getWorkoutType();
        this.status = workout.getStatus();
        this.day = workout.getStartTime().getDayOfWeek();
        this.duration = workout.getDurationSec();
        this.week = workout.getStartTime().getDayOfWeek().getValue();
        this.source = workout.getSource();
    }

    // METHODS --------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }
    
}
