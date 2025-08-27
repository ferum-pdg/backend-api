package org.heigvd.dto.training_plan_dto;

import org.heigvd.entity.Goal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TrainingPlanRequestDto {

    private LocalDate endDate;
    private List<String> daysOfWeek;
    private List<String> goals;
    // Could be used in the future to start the plan later
    private final boolean startNow = true;

    // CONSTRUCTORS ---------------------------------------------
    public TrainingPlanRequestDto() {}

    public TrainingPlanRequestDto(LocalDate endDate, List<String> daysOfWeek, List<String> goals) {
        this.endDate = endDate;
        this.daysOfWeek = daysOfWeek;
        this.goals = goals;
    }

    public TrainingPlanRequestDto(LocalDate endDate, List<DayOfWeek> daysOfWeek, List<Goal> goals, boolean isWeird) {
        this.endDate = endDate;
        this.daysOfWeek = daysOfWeek.stream()
                .map(DayOfWeek::name)
                .toList();
        this.goals = goals.stream()
                .map(Goal::getId)
                .map(UUID::toString)
                .toList();
    }

    // METHODS --------------------------------------------------

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<String> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<String> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public List<String> getGoals() { return goals; }
    public List<UUID> getGoalIds() {
        return goals.stream()
                .map(UUID::fromString)
                .toList();
    }
    public void setGoals(List<String> goals) { this.goals = goals; }

    public boolean startNow() { return startNow; }
}
