package org.heigvd.dto.WorkoutDto.DataPointDto;

import java.time.OffsetDateTime;

public class WorkoutBPMDataPointDto {
    private OffsetDateTime timestamp;
    private Double bpm;

    // CONSTRUCTORS --------------------------------

    public WorkoutBPMDataPointDto() {}

    public WorkoutBPMDataPointDto(OffsetDateTime timestamp, Double bpm) {
        this.timestamp = timestamp;
        this.bpm = bpm;
    }

    // GETTERS AND SETTERS --------------------------

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public Double getBpm() { return bpm; }
    public void setBpm(Double bpm) { this.bpm = bpm; }
}
