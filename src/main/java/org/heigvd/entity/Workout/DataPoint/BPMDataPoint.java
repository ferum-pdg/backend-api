package org.heigvd.entity.Workout.DataPoint;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class BPMDataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private OffsetDateTime timestamp;

    private Double bpm;


    // CONSTRUCTORS --------------------------------

    public BPMDataPoint() {}

    public BPMDataPoint(OffsetDateTime timestamp, Double bpm) {
        this.timestamp = timestamp;
        this.bpm = bpm;
    }

    // METHODS --------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public Double getBpm() { return bpm; }
    public void setBpm(Double bpm) { this.bpm = bpm; }
}
