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
    private OffsetDateTime start;


    // CONSTRUCTORS --------------------------------

    public WorkoutLightDto() {}

    public WorkoutLightDto(Workout workout) {
        this.id = workout.getId();
        this.sport = workout.getSport();
        this.type = workout.getWorkoutType();
        this.status = workout.getStatus();
        this.day = workout.getStartTime().getDayOfWeek();
        this.duration = workout.getDurationSec();
        this.week = 0;
        this.source = workout.getSource();
        this.start = workout.getStartTime();
    }

    public WorkoutLightDto(Workout workout, Integer _week) {
        this.id = workout.getId();
        this.sport = workout.getSport();
        this.type = workout.getWorkoutType();
        this.status = workout.getStatus();
        this.day = workout.getStartTime().getDayOfWeek();
        this.duration = workout.getDurationSec();
        this.week = _week;
        this.source = workout.getSource();
        this.start = workout.getStartTime();
    }

    // METHODS --------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public WorkoutType getType() { return type; }
    public void setType(WorkoutType type) { this.type = type; }

    public WorkoutStatus getStatus() { return status; }
    public void setStatus(WorkoutStatus status) { this.status = status; }

    public DayOfWeek getDay() { return day; }
    public void setDay(DayOfWeek day) { this.day = day; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getWeek() { return week; }
    public void setWeek(Integer week) { this.week = week; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public OffsetDateTime getStart() { return start; }
    public void setStart(OffsetDateTime start) { this.start = start; }
    
}
