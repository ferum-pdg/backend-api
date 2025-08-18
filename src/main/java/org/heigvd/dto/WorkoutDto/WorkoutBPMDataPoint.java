package org.heigvd.dto.WorkoutDto;

import java.time.OffsetDateTime;

public class WorkoutBPMDataPoint {
    private OffsetDateTime timestamp;
    private Double bpm;

    // CONSTRUCTORS --------------------------------

    public WorkoutBPMDataPoint() {}

    public WorkoutBPMDataPoint(OffsetDateTime timestamp, Double bpm) {
        this.timestamp = timestamp;
        this.bpm = bpm;
    }

    // GETTERS AND SETTERS --------------------------

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public Double getBpm() { return bpm; }
    public void setBpm(Double bpm) { this.bpm = bpm; }
}
