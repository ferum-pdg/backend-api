package org.heigvd.dto.workout_dto;

import org.heigvd.dto.workout_dto.data_point_dto.WorkoutPerfDetailsDto;
import org.heigvd.entity.Sport;
import org.heigvd.entity.workout.WorkoutStatus;
import org.heigvd.entity.workout.WorkoutType;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class WorkoutFullDto {

    private UUID id;
    private Sport sport;
    private WorkoutType type;
    private WorkoutStatus status;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private DayOfWeek day;
    private int durationSec;
    private int avgHeartRate;
    private Double distanceMeters;
    private Double caloriesKcal;
    private Double grade;
    private String aiReview;
    private List<WorkoutPlanDto> plan;
    private List<WorkoutPerfDetailsDto> performanceDetails;

    // CONSTRUCTORS ----------------------

    public WorkoutFullDto() {}

    public WorkoutFullDto(UUID id, Sport sport, WorkoutType type, WorkoutStatus status,
                          OffsetDateTime start, OffsetDateTime end, DayOfWeek day, int durationSec,
                          int avgHeartRate, Double distanceMeters, Double caloriesKcal, Double grade,
                          String aiReview, List<WorkoutPlanDto> plan, List<WorkoutPerfDetailsDto> performanceDetails) {
        this.id = id;
        this.sport = sport;
        this.type = type;
        this.status = status;
        this.start = start;
        this.end = end;
        this.day = day;
        this.durationSec = durationSec;
        this.avgHeartRate = avgHeartRate;
        this.distanceMeters = distanceMeters;
        this.caloriesKcal = caloriesKcal;
        this.grade = grade;
        this.aiReview = aiReview;
        this.plan = plan;
        this.performanceDetails = performanceDetails;
    }

    public WorkoutFullDto(UUID id, Sport sport, WorkoutType type, WorkoutStatus status,
                          DayOfWeek day, int durationSec, List<WorkoutPlanDto> plan) {
        this.id = id;
        this.sport = sport;
        this.type = type;
        this.status = status;
        this.day = day;
        this.durationSec = durationSec;
        this.plan = plan;
    }

    // GETTERS & SETTERS ----------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public WorkoutType getType() { return type; }
    public void setType(WorkoutType type) { this.type = type; }

    public WorkoutStatus getStatus() { return status; }
    public void setStatus(WorkoutStatus status) { this.status = status; }

    public OffsetDateTime getStart() { return start; }
    public void setStart(OffsetDateTime start) { this.start = start; }

    public OffsetDateTime getEnd() { return end; }
    public void setEnd(OffsetDateTime end) { this.end = end; }

    public DayOfWeek getDay() { return day; }
    public void setDay(DayOfWeek day) { this.day = day; }

    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }

    public int getAvgHeartRate() { return avgHeartRate; }
    public void setAvgHeartRate(int avgHeartRate) { this.avgHeartRate = avgHeartRate; }

    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

    public Double getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(Double caloriesKcal) { this.caloriesKcal = caloriesKcal; }

    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }

    public String getAiReview() { return aiReview; }
    public void setAiReview(String aiReview) { this.aiReview = aiReview; }

    public List<WorkoutPlanDto> getPlan() { return plan; }
    public void setPlan(List<WorkoutPlanDto> plan) { this.plan = plan; }

    public List<WorkoutPerfDetailsDto> getPerformanceDetails() { return performanceDetails; }
    public void setPerformanceDetails(List<WorkoutPerfDetailsDto> performanceDetails) { this.performanceDetails = performanceDetails; }
}