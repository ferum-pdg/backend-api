package org.heigvd.dto.workout_dto.data_point_dto;

public class WorkoutPerfDetailsDto {

    private int blocId;
    private int plannedBPMMin;
    private int plannedBPMMax;
    private int actualBPMMean;

    // CONSTRUCTORS ----------------------

    public WorkoutPerfDetailsDto() { }

    public WorkoutPerfDetailsDto(int blocId, int plannedBPMMin, int plannedBPMMax, int actualBPMMean) {
        this.blocId = blocId;
        this.plannedBPMMin = plannedBPMMin;
        this.plannedBPMMax = plannedBPMMax;
        this.actualBPMMean = actualBPMMean;
    }

    // GETTERS & SETTERS ----------------------

    public int getBlocId() { return blocId; }
    public void setBlocId(int blocId) { this.blocId = blocId; }

    public int getPlannedBPMMin() { return plannedBPMMin; }
    public void setPlannedBPMMin(int plannedBPMMin) { this.plannedBPMMin = plannedBPMMin; }

    public int getPlannedBPMMax() { return plannedBPMMax; }
    public void setPlannedBPMMax(int plannedBPMMax) { this.plannedBPMMax = plannedBPMMax; }

    public int getActualBPMMean() { return actualBPMMean; }
    public void setActualBPMMean(int actualBPMMean) { this.actualBPMMean = actualBPMMean; }
}
