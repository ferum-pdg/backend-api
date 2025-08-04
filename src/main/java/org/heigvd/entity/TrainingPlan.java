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

    @ManyToOne
    private Account account;


    // CONSTRUCTORS ---------------------------------------------

    public TrainingPlan() {}

    public TrainingPlan(Goal _goal, LocalDate _endDate, List<DayOfWeek> _daysOfWeek, List<DayOfWeek> _longOutgoing, Account _account) {
        this(List.of(_goal), _endDate, _daysOfWeek, _longOutgoing, _account);
    }


    public TrainingPlan(List<Goal> _goals, LocalDate _endDate, List<DayOfWeek> _daysOfWeek, List<DayOfWeek> _longOutgoing, Account _account) {
        this.goals = _goals;
        this.endDate = _endDate;
        this.daysOfWeek = _daysOfWeek;
        this.longOutgoing = _longOutgoing;
        this.account = _account;
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
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    public List<DayOfWeek> getLongOutgoing() {
        return longOutgoing;
    }
    public void setLongOutgoing(List<DayOfWeek> longOutgoing) {
        this.longOutgoing = longOutgoing;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
    public Account getAccount() {
        return account;
    }
    public void setAccount(Account account) {
        this.account = account;
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
