package org.heigvd.entity.Workout;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class WorkoutDataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private Workout workout;

    @Column(name = "timestamp")
    private OffsetDateTime timestamp;

    @Column(name = "speed_mps")
    private Double speedMps;

    @Column(name = "heart_rate_bpm")
    private Integer heartRate;

    public WorkoutDataPoint() {}

    public WorkoutDataPoint(Workout workout, OffsetDateTime timestamp, Double speedMps, Integer heartRate) {
        this.workout = workout;
        this.timestamp = timestamp;
        this.speedMps = speedMps;
        this.heartRate = heartRate;
    }

}