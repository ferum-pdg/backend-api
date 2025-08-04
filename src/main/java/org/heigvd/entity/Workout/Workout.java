package org.heigvd.entity.Workout;

import jakarta.persistence.*;
import org.heigvd.entity.Account;
import org.heigvd.entity.Sport;
import org.heigvd.entity.TrainingPlan;
import org.heigvd.entity.TrainingStatus;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
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

    // METHODS --------------------------------------------------

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
