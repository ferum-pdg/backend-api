package org.heigvd.dto.workout_dto.data_point_dto;

public class WorkoutPerfDetailsDto {

    private int blocId;
    private double plannedBPMMin;
    private double plannedBPMMax;
    private double actualBPMMean;

    // CONSTRUCTORS ----------------------

    public WorkoutPerfDetailsDto() { }

    public WorkoutPerfDetailsDto(int blocId, double plannedBPMMin, double plannedBPMMax, double actualBPMMean) {
        this.blocId = blocId;
        this.plannedBPMMin = plannedBPMMin;
        this.plannedBPMMax = plannedBPMMax;
        this.actualBPMMean = actualBPMMean;
    }

    // GETTERS & SETTERS ----------------------

    public int getBlocId() { return blocId; }
    public void setBlocId(int blocId) { this.blocId = blocId; }

    public double getPlannedBPMMin() { return plannedBPMMin; }
    public void setPlannedBPMMin(double plannedBPMMin) { this.plannedBPMMin = plannedBPMMin; }

    public double getPlannedBPMMax() { return plannedBPMMax; }
    public void setPlannedBPMMax(double plannedBPMMax) { this.plannedBPMMax = plannedBPMMax; }

    public double getActualBPMMean() { return actualBPMMean; }
    public void setActualBPMMean(double actualBPMMean) { this.actualBPMMean = actualBPMMean; }

    @Override
    public String toString() {
        return "WorkoutPerfDetailsDto{" +
                "blocId=" + blocId +
                ", plannedBPMMin=" + plannedBPMMin +
                ", plannedBPMMax=" + plannedBPMMax +
                ", actualBPMMean=" + actualBPMMean +
                '}';
    }
}
