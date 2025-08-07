package org.heigvd.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Entity
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = true)
    private Account account;

    @ManyToOne
    private TrainingPlan trainingPlan;

    private Sport sport;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "duration_sec")
    private int durationSec;

    @Column(name = "distance_meters")
    private double distanceMeters;

    @Column(name = "calories_kcal")
    private double caloriesKcal;

    @Column(name = "avg_heart_rate")
    private int avgHeartRate;

    @Column(name = "max_heart_rate")
    private int maxHeartRate;

    @Column(name = "average_speed")
    private Double averageSpeed;

    private String source;

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

    // GETTERS AND SETTERS --------------------------------------

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public TrainingPlan getTrainingPlan() {
        return trainingPlan;
    }

    public Sport getSport() {
        return sport;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public double getCaloriesKcal() {
        return caloriesKcal;
    }

    public int getAvgHeartRate() {
        return avgHeartRate;
    }

    public int getMaxHeartRate() {
        return maxHeartRate;
    }

    public Double getAverageSpeed() {
        return averageSpeed;
    }

    public String getSource() {
        return source;
    }

    public TrainingStatus getStatus() {
        return status;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setTrainingPlan(TrainingPlan trainingPlan) {
        this.trainingPlan = trainingPlan;
    }

    public void setSport(Sport sport) {
        this.sport = sport;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public void setDistanceMeters(double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public void setCaloriesKcal(double caloriesKcal) {
        this.caloriesKcal = caloriesKcal;
    }

    public void setAvgHeartRate(int avgHeartRate) {
        this.avgHeartRate = avgHeartRate;
    }

    public void setMaxHeartRate(int maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }

    public void setAverageSpeed(Double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setStatus(TrainingStatus status) {
        this.status = status;
    }

    // UTILITY METHODS ------------------------------------------

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
