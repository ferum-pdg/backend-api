package org.heigvd.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
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
}
