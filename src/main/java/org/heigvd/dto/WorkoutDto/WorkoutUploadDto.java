package org.heigvd.dto.WorkoutDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class WorkoutUploadDto {
    private UUID id;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private String sport;
    private Double distance;
    private Double avgSpeed;
    private Double avgBPM;
    private Double maxBPM;
    private List<WorkoutBPMDataPoint> bpmDataPoints;
    private List<WorkoutSpeedDataPoint> speedDataPoints;
    private String source;

    // CONSTRUCTORS ----------------------------------------

    public WorkoutUploadDto() {}

    public WorkoutUploadDto(UUID id, OffsetDateTime start, OffsetDateTime end, String sport,
                            Double distance, Double avgSpeed, Double avgBPM, Double maxBPM,
                            List<WorkoutBPMDataPoint> bpmDataPoints,
                            List<WorkoutSpeedDataPoint> speedDataPoints, String source) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.sport = sport;
        this.distance = distance;
        this.avgSpeed = avgSpeed;
        this.avgBPM = avgBPM;
        this.maxBPM = maxBPM;
        this.bpmDataPoints = bpmDataPoints;
        this.speedDataPoints = speedDataPoints;
        this.source = source;
    }

    // GETTERS AND SETTERS ---------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OffsetDateTime getStart() { return start; }
    public void setStart(OffsetDateTime start) { this.start = start; }

    public OffsetDateTime getEnd() { return end; }
    public void setEnd(OffsetDateTime end) { this.end = end; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Double getAvgSpeed() { return avgSpeed; }
    public void setAvgSpeed(Double avgSpeed) { this.avgSpeed = avgSpeed; }

    public Double getAvgBPM() { return avgBPM; }
    public void setAvgBPM(Double avgBPM) { this.avgBPM = avgBPM; }

    public Double getMaxBPM() { return maxBPM; }
    public void setMaxBPM(Double maxBPM) { this.maxBPM = maxBPM; }

    public List<WorkoutBPMDataPoint> getBpmDataPoints() { return bpmDataPoints; }
    public void setBpmDataPoints(List<WorkoutBPMDataPoint> bpmDataPoints) { this.bpmDataPoints = bpmDataPoints; }

    public List<WorkoutSpeedDataPoint> getSpeedDataPoints() { return speedDataPoints; }
    public void setSpeedDataPoints(List<WorkoutSpeedDataPoint> speedDataPoints) { this.speedDataPoints = speedDataPoints; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
