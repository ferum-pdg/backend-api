package org.heigvd.dto.workout_dto;

import org.heigvd.entity.workout.IntensityZone;

public class WorkoutPlanDetailsDto {

    private int blocDetailId;
    private int durationSec;
    private int bpmMinTarget;
    private int bpmMaxTarget;
    private IntensityZone intensityZone;

    // CONSTRUCTORS ----------------------
    public WorkoutPlanDetailsDto() {}

    public WorkoutPlanDetailsDto(int blocDetailId, int durationSec, int bpmMinTarget, int bpmMaxTarget, IntensityZone intensityZone) {
        this.blocDetailId = blocDetailId;
        this.durationSec = durationSec;
        this.bpmMinTarget = bpmMinTarget;
        this.bpmMaxTarget = bpmMaxTarget;
        this.intensityZone = intensityZone;
    }

    // GETTERS & SETTERS ----------------------

    public int getBlocDetailId() { return blocDetailId; }
    public void setBlocDetailId(int blocDetailId) { this.blocDetailId = blocDetailId; }

    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }

    public int getBpmMinTarget() { return bpmMinTarget; }
    public void setBpmMinTarget(int bpmMinTarget) { this.bpmMinTarget = bpmMinTarget; }

    public int getBpmMaxTarget() { return bpmMaxTarget; }
    public void setBpmMaxTarget(int bpmMaxTarget) { this.bpmMaxTarget = bpmMaxTarget; }

    public IntensityZone getIntensityZone() { return intensityZone; }
    public void setIntensityZone(IntensityZone intensityZone) { this.intensityZone = intensityZone; }
}
