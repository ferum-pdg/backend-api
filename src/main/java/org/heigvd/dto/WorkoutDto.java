package org.heigvd.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.UUID;

public class WorkoutDto {

    private UUID id;

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    private UUID trainingPlanId;

    @NotNull(message = "Sport is required")
    private String sport;

    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    @Min(value = 0, message = "Duration must be positive")
    private Integer durationSec;

    @DecimalMin(value = "0.0", message = "Distance must be positive")
    private Double distanceMeters;

    @DecimalMin(value = "0.0", message = "Calories must be positive")
    private Double caloriesKcal;

    @Min(value = 0, message = "Heart rate must be positive")
    @Max(value = 220, message = "Heart rate cannot exceed 220")
    private Integer avgHeartRate;

    @Min(value = 0, message = "Heart rate must be positive")
    @Max(value = 220, message = "Heart rate cannot exceed 220")
    private Integer maxHeartRate;

    @DecimalMin(value = "0.0", message = "Speed must be positive")
    private Double averageSpeed;

    private String source;

    @NotNull(message = "Status is required")
    private String status;

    // CONSTRUCTORS
    public WorkoutDto() {}

    public WorkoutDto(UUID id, UUID accountId, UUID trainingPlanId, String sport,
                      OffsetDateTime startTime, OffsetDateTime endTime, Integer durationSec,
                      Double distanceMeters, Double caloriesKcal, Integer avgHeartRate,
                      Integer maxHeartRate, Double averageSpeed, String source, String status) {
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
        this.averageSpeed = averageSpeed;
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

    public Double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(Double averageSpeed) { this.averageSpeed = averageSpeed; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}