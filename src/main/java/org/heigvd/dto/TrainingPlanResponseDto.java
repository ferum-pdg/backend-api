package org.heigvd.dto;

import org.heigvd.entity.Goal;
import org.heigvd.entity.TrainingPlan.DailyPlan;
import org.heigvd.entity.TrainingPlan.TrainingPlan;
import org.heigvd.entity.TrainingPlan.TrainingPlanPhase;
import org.heigvd.entity.TrainingPlan.WeeklyPlan;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TrainingPlanResponseDto {

    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DayOfWeek> daysOfWeek;
    private List<WeeklyPlan> dailyPlans;
    private List<Goal> goals;
    private TrainingPlanPhase phase;

    // CONSTRUCTORS ---------------------------------------------

    public TrainingPlanResponseDto() {}

    public TrainingPlanResponseDto(TrainingPlan tp) {
        this.id = tp.getId();
        this.startDate = tp.getStartDate();
        this.endDate = tp.getEndDate();
        this.daysOfWeek = tp.getDaysOfWeek();
        this.dailyPlans = tp.getWeeklyPlans();
        this.goals = tp.getGoals();
    }

    // METHODS --------------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<DayOfWeek> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public List<WeeklyPlan> getDailyPlans() { return dailyPlans; }
    public void setDailyPlans(List<WeeklyPlan> dailyPlans) { this.dailyPlans = dailyPlans; }

    public List<Goal> getGoals() { return goals; }
    public void setGoals(List<Goal> goals) { this.goals = goals; }

}
