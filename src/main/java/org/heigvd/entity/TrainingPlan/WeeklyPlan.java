package org.heigvd.entity.TrainingPlan;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class WeeklyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<DailyPlan> dailyPlans = new ArrayList<>();

    @Column(name = "week_number")
    private Integer weekNumber;

    @Enumerated(EnumType.STRING)
    private TrainingPlanPhase phase;

    // CONSTRUCTORS ---------------------------------------------

    public WeeklyPlan() {}

    public WeeklyPlan(List<DailyPlan> dailyPlans, Integer weekNumber, TrainingPlanPhase phase) {
        this.dailyPlans = dailyPlans;
        this.weekNumber = weekNumber;
        this.phase = phase;
    }

    // METHODS --------------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public List<DailyPlan> getDailyPlans() { return dailyPlans; }
    public void setDailyPlans(List<DailyPlan> dailyPlans) { this.dailyPlans = dailyPlans; }

    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    @Override
    public String toString() {
        return " {\n" + "\n" +
                "  id=" + id + "\n" +
                "  dailyPlans=" + dailyPlans + "\n" +
                "  weekNumber=" + weekNumber + "\n" +
                "  phase=" + phase + "\n" +
                " }";
    }
}
