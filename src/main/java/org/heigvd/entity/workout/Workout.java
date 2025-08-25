package org.heigvd.entity.workout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.heigvd.dto.workout_dto.data_point_dto.WorkoutBPMDataPointDto;
import org.heigvd.dto.workout_dto.data_point_dto.WorkoutSpeedDataPointDto;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.workout.data_point.BPMDataPoint;
import org.heigvd.entity.workout.data_point.SpeedDataPoint;

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

    @JsonIgnore
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

    @Column(name = "avg_speed")
    private Double avgSpeed;

    private String source;

    @Enumerated(EnumType.STRING)
    private WorkoutStatus status;

    @Enumerated(EnumType.STRING)
    private WorkoutType type;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BPMDataPoint> actualBPMDataPoints = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SpeedDataPoint> actualSpeedDataPoints = new ArrayList<>();

    // CONSTRUCTORS ---------------------------------------------

    public Workout() {}

    public Workout(Account account, Sport sport, OffsetDateTime startTime,
                   OffsetDateTime endTime, String source, WorkoutStatus status,
                   WorkoutType type) {
        this.account = account;
        this.sport = sport;
        this.startTime = startTime;
        this.endTime = endTime;
        this.source = source;
        this.status = status;
        this.type = type;
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

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public WorkoutStatus getStatus() { return status; }
    public void setStatus(WorkoutStatus status) { this.status = status; }

    public WorkoutType getWorkoutType() { return type; }
    public void setWorkoutType(WorkoutType type) { this.type = type; }

    public Double getAvgSpeed() { return avgSpeed; }
    public void setAvgSpeed(Double avgSpeed) { this.avgSpeed = avgSpeed; }

    public List<BPMDataPoint> getActualBPMDataPoints() { return actualBPMDataPoints; }
    public void setActualBPMDataPoints(List<WorkoutBPMDataPointDto> bpmDataPoints) {
        this.actualBPMDataPoints.clear();
        for (WorkoutBPMDataPointDto dto : bpmDataPoints) {
            BPMDataPoint dataPoint = new BPMDataPoint();
            dataPoint.setTimestamp(dto.getTs());
            dataPoint.setBpm(dto.getBpm());
            this.actualBPMDataPoints.add(dataPoint);
        }
    }

    public List<SpeedDataPoint> getActualSpeedDataPoints() { return actualSpeedDataPoints; }
    public void setActualSpeedDataPoints(List<WorkoutSpeedDataPointDto> speedDataPoints) {
        this.actualSpeedDataPoints.clear();
        for (WorkoutSpeedDataPointDto dto : speedDataPoints) {
            SpeedDataPoint dataPoint = new SpeedDataPoint();
            dataPoint.setTimestamp(dto.getTs());
            dataPoint.setKmh(dto.getKmh());
            dataPoint.setPaceMinPerKm(dto.getPaceMinPerKm());
            this.actualSpeedDataPoints.add(dataPoint);
        }
    }

    @Override
    public String toString() {
        return " {" + "\n" +
                "   id = " + id + ",\n" +
                "   sport = " + sport +",\n" +
                "   startTime = " + startTime.format(DateTimeFormatter.ofPattern("EEEE d MMMM 'at' HH:mm")) + ",\n" +
                "   status = " + status +",\n" +
                " }" + "\n";
    }
}
