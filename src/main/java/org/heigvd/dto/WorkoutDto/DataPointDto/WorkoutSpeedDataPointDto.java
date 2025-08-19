package org.heigvd.dto.WorkoutDto.DataPointDto;

import java.time.OffsetDateTime;

public class WorkoutSpeedDataPointDto {

    private OffsetDateTime timestamp;
    private Double kmh;
    private Double pace_min_per_km;

    // CONSTRUCTORS --------------------------------

    public WorkoutSpeedDataPointDto() {}

    public WorkoutSpeedDataPointDto(OffsetDateTime timestamp, Double kmh, Double pace_min_per_km) {
        this.timestamp = timestamp;
        this.kmh = kmh;
        this.pace_min_per_km = pace_min_per_km;
    }

    // GETTERS AND SETTERS --------------------------

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }

    public Double getKmh() { return kmh; }
    public void setKmh(Double kmh) { this.kmh = kmh; }

    public Double getPaceMinPerKm() { return pace_min_per_km; }
    public void setPaceMinPerKm(Double pace_min_per_km) { this.pace_min_per_km = pace_min_per_km; }
}
