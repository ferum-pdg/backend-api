package org.heigvd.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "training_plan_id")
    private TrainingPlan trainingPlan;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Sport sport;

    @NotNull
    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Min(0)
    @Column(name = "duration_sec")
    private int durationSec;

    @DecimalMin("0.0")
    @Column(name = "distance_meters")
    private double distanceMeters;

    @DecimalMin("0.0")
    @Column(name = "calories_kcal")
    private double caloriesKcal;

    @Min(0)
    @Max(220)
    @Column(name = "avg_heart_rate")
    private int avgHeartRate;

    @Min(0)
    @Max(220)
    @Column(name = "max_heart_rate")
    private int maxHeartRate;

    @DecimalMin("0.0")
    @Column(name = "average_speed")
    private Double averageSpeed;

    private String source;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TrainingStatus status;

    // CONSTRUCTORS ---------------------------------------------

    public Workout() {}

    public Workout(Account account, TrainingPlan trainingPlan, Sport sport, OffsetDateTime startTime,
                   OffsetDateTime endTime, int durationSec, double distanceMeters, double caloriesKcal,
                   int avgHeartRate, int maxHeartRate, Double averageSpeed, String source, TrainingStatus status) {
        this.account = account;
        this.trainingPlan = trainingPlan;
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

    public Workout(Account account, TrainingPlan trainingPlan, Sport sport, OffsetDateTime startTime,
                   OffsetDateTime endTime, String source, TrainingStatus status) {
        this.account = account;
        this.trainingPlan = trainingPlan;
        this.sport = sport;
        this.startTime = startTime;
        this.endTime = endTime;
        this.source = source;
        this.status = status;
    }

    // GETTERS & SETTERS ----------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public TrainingPlan getTrainingPlan() { return trainingPlan; }
    public void setTrainingPlan(TrainingPlan trainingPlan) { this.trainingPlan = trainingPlan; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }

    public double getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(double caloriesKcal) { this.caloriesKcal = caloriesKcal; }

    public int getAvgHeartRate() { return avgHeartRate; }
    public void setAvgHeartRate(int avgHeartRate) { this.avgHeartRate = avgHeartRate; }

    public int getMaxHeartRate() { return maxHeartRate; }
    public void setMaxHeartRate(int maxHeartRate) { this.maxHeartRate = maxHeartRate; }

    public Double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(Double averageSpeed) { this.averageSpeed = averageSpeed; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public TrainingStatus getStatus() { return status; }
    public void setStatus(TrainingStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "{" + ",\n" +
                "   id = " + id + ",\n" +
                "   sport = " + sport +",\n" +
                "   startTime = " + startTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM 'at' HH:mm")) + ",\n" +
                "   status = " + status +",\n" +
                "}," + "\n";
    }
}