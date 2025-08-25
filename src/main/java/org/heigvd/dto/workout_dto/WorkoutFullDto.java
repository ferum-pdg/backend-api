package org.heigvd.dto.workout_dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class WorkoutFullDto {

    private UUID id;

    private UUID accountId;

    private UUID trainingPlanId;

    private String sport;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private Integer durationSec;

    private Double distanceMeters;

    private Double caloriesKcal;

    private Integer avgHeartRate;

    private Integer maxHeartRate;

    private Double avgSpeed;

    private String source;

    private String status;

    // CONSTRUCTORS
    public WorkoutFullDto() {}

    public WorkoutFullDto(UUID id, UUID accountId, UUID trainingPlanId, String sport,
                          OffsetDateTime startTime, OffsetDateTime endTime, Integer durationSec,
                          Double distanceMeters, Double caloriesKcal, Integer avgHeartRate,
                          Integer maxHeartRate, Double avgSpeed, String source, String status) {
        this.id = id;
        this.accountId = accountId;
        this.trainingPlanId = trainingPlanId;
        this.sport = sport;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSec = durationSec;
        this.distanceMeters = distanceMeters;
        this.caloriesKcal = caloriesKcal;
        this.avgHeartRate = avgHeartRate;
        this.maxHeartRate = maxHeartRate;
        this.avgSpeed = avgSpeed;
        this.source = source;
        this.status = status;
    }

    // GETTERS & SETTERS
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public UUID getTrainingPlanId() { return trainingPlanId; }
    public void setTrainingPlanId(UUID trainingPlanId) { this.trainingPlanId = trainingPlanId; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }

    public Double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

    public Double getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(Double caloriesKcal) { this.caloriesKcal = caloriesKcal; }

    public Integer getAvgHeartRate() { return avgHeartRate; }
    public void setAvgHeartRate(Integer avgHeartRate) { this.avgHeartRate = avgHeartRate; }

    public Integer getMaxHeartRate() { return maxHeartRate; }
    public void setMaxHeartRate(Integer maxHeartRate) { this.maxHeartRate = maxHeartRate; }

    public Double getAvgSpeed() { return avgSpeed; }
    public void setAvgSpeed(Double avgSpeed) { this.avgSpeed = avgSpeed; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}