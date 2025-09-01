package org.heigvd.dto.workout_dto.data_point_dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class WorkoutSpeedDataPointDto {

    private OffsetDateTime ts;
    private Double kmh;

    @JsonProperty("pace_min_per_km")
    private Double pace_min_per_km;

    // CONSTRUCTORS --------------------------------

    public WorkoutSpeedDataPointDto() {}

    public WorkoutSpeedDataPointDto(OffsetDateTime ts, Double kmh, Double pace_min_per_km) {
        this.ts = ts;
        this.kmh = kmh;
        this.pace_min_per_km = pace_min_per_km;
    }

    // GETTERS AND SETTERS --------------------------

    public OffsetDateTime getTs() { return ts; }
    public void setTs(OffsetDateTime ts) { this.ts = ts; }

    public Double getKmh() { return kmh; }
    public void setKmh(Double kmh) { this.kmh = kmh; }

    public Double getPaceMinPerKm() { return pace_min_per_km; }
    public void setPaceMinPerKm(Double pace_min_per_km) { this.pace_min_per_km = pace_min_per_km; }
}
