package org.heigvd.dto.training_plan_dto;

import org.heigvd.entity.training_plan.WeeklyPlan;

import java.util.UUID;

public class TrainingPlanLightDto {

    private UUID id;
    private Integer currentWeekNb;
    private Integer totalNbOfWeeks;
    private Integer currentNbOfWorkouts;
    private Integer totalNbOfWorkouts;
    private WeeklyPlan currentWeeklyPlan;

    // CONSTRUCTORS ---------------------------------------------

    public TrainingPlanLightDto() {}

    public TrainingPlanLightDto(UUID id, Integer currentWeekNb,
                                Integer totalNbOfWeeks, Integer currentNbOfWorkouts,
                                Integer totalNbOfWorkouts, WeeklyPlan currentWeeklyPlan) {
        this.id = id;
        this.currentWeekNb = currentWeekNb;
        this.totalNbOfWeeks = totalNbOfWeeks;
        this.currentNbOfWorkouts = currentNbOfWorkouts;
        this.totalNbOfWorkouts = totalNbOfWorkouts;
        this.currentWeeklyPlan = currentWeeklyPlan;
    }

    // METHODS --------------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getCurrentWeekNb() { return currentWeekNb; }
    public void setCurrentWeekNb(Integer currentWeekNb) { this.currentWeekNb = currentWeekNb; }

    public Integer getTotalNbOfWeeks() { return totalNbOfWeeks; }
    public void setTotalNbOfWeeks(Integer totalNbOfWeeks) { this.totalNbOfWeeks = totalNbOfWeeks; }

    public Integer getCurrentNbOfWorkouts() { return currentNbOfWorkouts; }
    public void setCurrentNbOfWorkouts(Integer currentNbOfWorkouts) { this.currentNbOfWorkouts = currentNbOfWorkouts; }

    public Integer getTotalNbOfWorkouts() { return totalNbOfWorkouts; }
    public void setTotalNbOfWorkouts(Integer totalNbOfWorkouts) { this.totalNbOfWorkouts = totalNbOfWorkouts; }

    public WeeklyPlan getCurrentWeeklyPlan() { return currentWeeklyPlan; }
    public void setCurrentWeeklyPlan(WeeklyPlan currentWeeklyPlan) { this.currentWeeklyPlan = currentWeeklyPlan; }

}
