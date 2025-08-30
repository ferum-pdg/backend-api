package org.heigvd.dto.workout_dto;

import org.heigvd.entity.workout.details.WorkoutPlan;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanDto {

    private int blocId;
    private int repetitionCount;
    private List<WorkoutPlanDetailsDto> details;

    // CONSTRUCTORS ----------------------

    public WorkoutPlanDto() {}

    public WorkoutPlanDto(int blocId, int repetitionCount, List<WorkoutPlanDetailsDto> details) {
        this.blocId = blocId;
        this.repetitionCount = repetitionCount;
        this.details = details;
    }

    public WorkoutPlanDto(WorkoutPlan workoutPlan) {
        this.blocId = workoutPlan.getBlocId();
        this.repetitionCount = workoutPlan.getRepetitionCount();
    }

    // GETTERS & SETTERS ----------------------

    public int getBlocId() { return blocId; }
    public void setBlocId(int blocId) { this.blocId = blocId; }

    public int getRepetitionCount() { return repetitionCount; }
    public void setRepetitionCount(int repetitionCount) { this.repetitionCount = repetitionCount; }

    public List<WorkoutPlanDetailsDto> getDetails() { return details; }
    public void setDetails(List<WorkoutPlanDetailsDto> details) { this.details = details; }

}
