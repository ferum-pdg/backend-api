package org.heigvd.entity.Workout.DataPoint;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class SpeedDataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private OffsetDateTime timestamp;

    private Double kmh;

    @Column(name = "pace_min_per_km")
    private Double paceMinPerKm;

    // CONSTRUCTORS --------------------------------

    public SpeedDataPoint() {}

    public SpeedDataPoint(OffsetDateTime timestamp, Double kmh, Double paceMinPerKm) {
        this.timestamp = timestamp;
        this.kmh = kmh;
        this.paceMinPerKm = paceMinPerKm;
    }

    // METHODS --------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public Double getKmh() { return kmh; }
    public void setKmh(Double kmh) { this.kmh = kmh; }

    public Double getPaceMinPerKm() { return paceMinPerKm; }
    public void setPaceMinPerKm(Double paceMinPerKm) { this.paceMinPerKm = paceMinPerKm; }

}
