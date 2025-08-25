package org.heigvd.dto.WorkoutDto;

import jakarta.validation.constraints.NotNull;
import org.heigvd.dto.WorkoutDto.DataPointDto.WorkoutBPMDataPointDto;
import org.heigvd.dto.WorkoutDto.DataPointDto.WorkoutSpeedDataPointDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class WorkoutUploadDto {

    @NotNull
    private OffsetDateTime start;

    @NotNull
    private OffsetDateTime end;

    @NotNull
    private String sport;

    @NotNull
    private Double distance;

    @NotNull
    private Double caloriesKcal;

    @NotNull
    private Double avgSpeed;

    @NotNull
    private Double avgBPM;

    @NotNull
    private Double maxBPM;

    private List<WorkoutBPMDataPointDto> bpmDataPoints;
    private List<WorkoutSpeedDataPointDto> speedDataPoints;

    private String source;

    // CONSTRUCTORS ----------------------------------------

    public WorkoutUploadDto() {}

    public WorkoutUploadDto(OffsetDateTime start, OffsetDateTime end, String sport, Double distance,
                            Double caloriesKcal, Double avgSpeed, Double avgBPM, Double maxBPM,
                            List<WorkoutBPMDataPointDto> bpmDataPoints,
                            List<WorkoutSpeedDataPointDto> speedDataPoints, String source) {
        this.start = start;
        this.end = end;
        this.sport = sport;
        this.distance = distance;
        this.caloriesKcal = caloriesKcal;
        this.avgSpeed = avgSpeed;
        this.avgBPM = avgBPM;
        this.maxBPM = maxBPM;
        this.bpmDataPoints = bpmDataPoints;
        this.speedDataPoints = speedDataPoints;
        this.source = source;
    }

    // GETTERS AND SETTERS ---------------------------------

    public OffsetDateTime getStart() { return start; }
    public void setStart(OffsetDateTime start) { this.start = start; }

    public OffsetDateTime getEnd() { return end; }
    public void setEnd(OffsetDateTime end) { this.end = end; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Double getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(Double caloriesKcal) { this.caloriesKcal = caloriesKcal; }

    public Double getAvgSpeed() { return avgSpeed; }
    public void setAvgSpeed(Double avgSpeed) { this.avgSpeed = avgSpeed; }

    public Double getAvgBPM() { return avgBPM; }
    public void setAvgBPM(Double avgBPM) { this.avgBPM = avgBPM; }

    public Double getMaxBPM() { return maxBPM; }
    public void setMaxBPM(Double maxBPM) { this.maxBPM = maxBPM; }

    public List<WorkoutBPMDataPointDto> getBpmDataPoints() { return bpmDataPoints; }
    public void setBpmDataPoints(List<WorkoutBPMDataPointDto> bpmDataPoints) { this.bpmDataPoints = bpmDataPoints; }

    public List<WorkoutSpeedDataPointDto> getSpeedDataPoints() { return speedDataPoints; }
    public void setSpeedDataPoints(List<WorkoutSpeedDataPointDto> speedDataPoints) { this.speedDataPoints = speedDataPoints; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
