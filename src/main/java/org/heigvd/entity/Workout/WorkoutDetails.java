package org.heigvd.entity.Workout;

import jakarta.persistence.*;
import org.heigvd.dto.WorkoutDto.WorkoutUploadDto;
import org.heigvd.entity.Workout.DataPoint.BPMDataPoint;
import org.heigvd.entity.Workout.DataPoint.SpeedDataPoint;

import java.util.List;
import java.util.UUID;

@Entity
public class WorkoutDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany
    private List<BPMDataPoint> plannedBPMDataPoints;

    @OneToMany
    private List<SpeedDataPoint> plannedSpeedDataPoints;

    @OneToMany
    private List<BPMDataPoint> actualBPMDataPoints;

    @OneToMany
    private List<SpeedDataPoint> actualSpeedDataPoints;

    // CONSTRUCTORS --------------------------------

    public WorkoutDetails() {}

    public WorkoutDetails(List<BPMDataPoint> plannedBPMDataPoints, List<SpeedDataPoint> plannedSpeedDataPoints) {
        this.plannedBPMDataPoints = plannedBPMDataPoints;
        this.plannedSpeedDataPoints = plannedSpeedDataPoints;
        this.actualBPMDataPoints = null;
        this.actualSpeedDataPoints = null;
    }

    public WorkoutDetails(List<BPMDataPoint> plannedBPMDataPoints, List<SpeedDataPoint> plannedSpeedDataPoints,
                          List<BPMDataPoint> actualBPMDataPoints, List<SpeedDataPoint> actualSpeedDataPoints) {
        this.plannedBPMDataPoints = plannedBPMDataPoints;
        this.plannedSpeedDataPoints = plannedSpeedDataPoints;
        this.actualBPMDataPoints = actualBPMDataPoints;
        this.actualSpeedDataPoints = actualSpeedDataPoints;
    }

    // METHODS --------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public List<BPMDataPoint> getPlannedBPMDataPoints() { return plannedBPMDataPoints; }
    public void setPlannedBPMDataPoints(List<BPMDataPoint> plannedBPMDataPoints) {
        this.plannedBPMDataPoints = plannedBPMDataPoints;
    }

    public List<SpeedDataPoint> getPlannedSpeedDataPoints() { return plannedSpeedDataPoints; }
    public void setPlannedSpeedDataPoints(List<SpeedDataPoint> plannedSpeedDataPoints) {
        this.plannedSpeedDataPoints = plannedSpeedDataPoints;
    }

    public List<BPMDataPoint> getActualBPMDataPoints() { return actualBPMDataPoints; }
    public void setActualBPMDataPoints(List<BPMDataPoint> actualBPMDataPoints) {
        this.actualBPMDataPoints = actualBPMDataPoints;
    }
    public void setActualBPMDataPointsFromDto(WorkoutUploadDto workoutUploadDto) {
        this.actualBPMDataPoints = workoutUploadDto.getBpmDataPoints().stream()
                .map(dto -> new BPMDataPoint(dto.getTimestamp(), dto.getBpm()))
                .toList();
    }

    public List<SpeedDataPoint> getActualSpeedDataPoints() { return actualSpeedDataPoints; }
    public void setActualSpeedDataPoints(List<SpeedDataPoint> actualSpeedDataPoints) {
        this.actualSpeedDataPoints = actualSpeedDataPoints;
    }
    public void setActualSpeedDataPointsFromDto(WorkoutUploadDto workoutUploadDto) {
        this.actualSpeedDataPoints = workoutUploadDto.getSpeedDataPoints().stream()
                .map(dto -> new SpeedDataPoint(dto.getTimestamp(), dto.getKmh(), dto.getPaceMinPerKm()))
                .toList();
    }
}
