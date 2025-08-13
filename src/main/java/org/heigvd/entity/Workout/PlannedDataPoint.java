package org.heigvd.entity.Workout;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class PlannedDataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private OffsetDateTime start;

    private OffsetDateTime end;

    @Column(name = "speed_mps")
    private Double speedMps;

    @Column(name = "heart_rate_bpm")
    private Integer heartRate;

    // CONSTRUCTORS ---------------------------------------------

    public PlannedDataPoint() {
    }

    public PlannedDataPoint(OffsetDateTime start, OffsetDateTime end, Double speedMps, Integer heartRate) {
        this.start = start;
        this.end = end;
        this.speedMps = speedMps;
        this.heartRate = heartRate;
    }

    // GETTERS & SETTERS -----------------------------------------

    @Override
    public String toString() {
        return "{" + "\n" +
                "    id=" + id + "\n" +
                "    start=" + start + "\n" +
                "    end=" + end + "\n" +
                "    speedMps=" + speedMps + "\n" +
                "    heartRate=" + heartRate + "\n" +
                "   }";
    }

}