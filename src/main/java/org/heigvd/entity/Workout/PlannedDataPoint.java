package org.heigvd.entity.Workout;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class PlannedDataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "speed_mps")
    private Double speedMps;

    @Column(name = "heart_rate_bpm")
    private Integer heartRate;

    // CONSTRUCTORS ---------------------------------------------

    public PlannedDataPoint() {
    }

    public PlannedDataPoint(OffsetDateTime start, OffsetDateTime end, Double speedMps, Integer heartRate) {
        this.startTime = start;
        this.endTime = end;
        this.speedMps = speedMps;
        this.heartRate = heartRate;
    }

    // GETTERS & SETTERS -----------------------------------------

    @Override
    public String toString() {
        return "{" + "\n" +
                "    id=" + id + "\n" +
                "    start=" + startTime + "\n" +
                "    end=" + endTime + "\n" +
                "    speedMps=" + speedMps + "\n" +
                "    heartRate=" + heartRate + "\n" +
                "   }";
    }

}