package org.heigvd.entity;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class TrainingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToMany
    @JoinTable(name = "training_plan_goals",
            joinColumns = @JoinColumn(name = "training_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "goal_id"))
    private List<Goal> goals;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "days_of_week")
    private List<DayOfWeek> daysOfWeek;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "long_outgoing")
    private List<DayOfWeek> longOutgoing;


    // CONSTRUCTORS ---------------------------------------------

    public TrainingPlan() {}

    public TrainingPlan(Goal _goal, LocalDate _startDate, List<DayOfWeek> _daysOfWeek, List<DayOfWeek> _longOutgoing) {
        this(List.of(_goal), _startDate, _daysOfWeek, _longOutgoing);
    }


    public TrainingPlan(List<Goal> _goals, LocalDate _startDate, List<DayOfWeek> _daysOfWeek, List<DayOfWeek> _longOutgoing) {
        this.goals = _goals;
        this.startDate = _startDate;
        this.endDate = _startDate.plusWeeks(_goals.stream().findFirst().get().getNbOfWeek());
        this.daysOfWeek = _daysOfWeek;
        this.longOutgoing = _longOutgoing;
    }

    // METHODS --------------------------------------------------

    public UUID getId() {
        return id;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    @Override
    public String toString() {
        return "TrainingPlan{" +
                "id=" + id +
                ", goals=" + goals +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", daysOfWeek=" + daysOfWeek +
                ", longOutgoing=" + longOutgoing +
                '}';
    }
}
