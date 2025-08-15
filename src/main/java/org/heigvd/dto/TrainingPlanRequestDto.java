package org.heigvd.dto;

import org.heigvd.entity.Goal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TrainingPlanRequestDto {

    private LocalDate endDate;
    private List<String> daysOfWeek;
    private List<String> goals;

    // CONSTRUCTORS ---------------------------------------------
    public TrainingPlanRequestDto() {}

    public TrainingPlanRequestDto(LocalDate endDate, List<String> daysOfWeek, List<String> goals) {
        this.endDate = endDate;
        this.daysOfWeek = daysOfWeek;
        this.goals = goals;
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
}
