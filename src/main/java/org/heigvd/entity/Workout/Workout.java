package org.heigvd.entity.Workout;

import jakarta.persistence.*;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
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
    private WorkoutStatus status;

    @OneToMany
    private List<PlannedDataPoint> plannedDataPoints = new ArrayList<>();

    @OneToMany
    private List<WorkoutDataPoint> actualDataPoints = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "workout_type")
    private WorkoutType workoutType;

    // CONSTRUCTORS ---------------------------------------------

    public Workout() {}

    public Workout(Account account, Sport sport, OffsetDateTime startTime,
                   OffsetDateTime endTime, String source, WorkoutStatus status, List<PlannedDataPoint> plannedDataPoints,
                   WorkoutType workoutType) {
        this.account = account;
        this.sport = sport;
        this.startTime = startTime;
        this.endTime = endTime;
        this.source = source;
        this.status = status;
        this.plannedDataPoints = plannedDataPoints;
        this.workoutType = workoutType;
        this.durationSec = (int) (endTime.toEpochSecond() - startTime.toEpochSecond());
    }

    // METHODS --------------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

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

    public WorkoutStatus getStatus() { return status; }
    public void setStatus(WorkoutStatus status) { this.status = status; }

    public List<PlannedDataPoint> getPlannedDataPoints() { return plannedDataPoints; }
    public void setPlannedDataPoints(List<PlannedDataPoint> plannedDataPoints) { this.plannedDataPoints = plannedDataPoints; }

    public List<WorkoutDataPoint> getActualDataPoints() { return actualDataPoints; }
    public void setActualDataPoints(List<WorkoutDataPoint> actualDataPoints) { this.actualDataPoints = actualDataPoints; }

    @Override
    public String toString() {
        return " {" + "\n" +
                "   id = " + id + ",\n" +
                "   sport = " + sport +",\n" +
                "   startTime = " + startTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM 'at' HH:mm")) + ",\n" +
                "   status = " + status +",\n" +
                "   plannedDataPoints = " + plannedDataPoints + "\n" +
                " }" + "\n";
    }
}
