package org.heigvd.dto.WorkoutDto.DataPointDto;

import java.time.OffsetDateTime;

public class WorkoutBPMDataPointDto {
    private OffsetDateTime ts;
    private Double bpm;

    // CONSTRUCTORS --------------------------------

    public WorkoutBPMDataPointDto() {}

    public WorkoutBPMDataPointDto(OffsetDateTime ts, Double bpm) {
        this.ts = ts;
        this.bpm = bpm;
    }

    // GETTERS AND SETTERS --------------------------

    public OffsetDateTime getTs() { return ts; }
    public void setTs(OffsetDateTime ts) { this.ts = ts; }

    public Double getBpm() { return bpm; }
    public void setBpm(Double bpm) { this.bpm = bpm; }
}
