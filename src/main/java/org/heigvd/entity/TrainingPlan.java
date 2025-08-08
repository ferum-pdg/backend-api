package org.heigvd.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
public class TrainingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @NotEmpty
    @ManyToMany
    @JoinTable(name = "training_plan_goals",
            joinColumns = @JoinColumn(name = "training_plan_id"),
            inverseJoinColumns = @JoinColumn(name = "goal_id"))
    private List<Goal> goals;

    @NotNull
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotEmpty
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "days_of_week")
    private List<DayOfWeek> daysOfWeek;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @Column(name = "long_outgoing")
    private List<DayOfWeek> longOutgoing;

    @OneToMany(mappedBy = "trainingPlan", cascade = CascadeType.ALL)
    private List<Workout> workouts;

    // CONSTRUCTORS ---------------------------------------------

    public TrainingPlan() {}

    public TrainingPlan(Goal _goal, LocalDate _startDate, List<DayOfWeek> _daysOfWeek, List<DayOfWeek> _longOutgoing) {
        this.goals = List.of(_goal);
        this.startDate = _startDate;
        this.endDate = _startDate.plusWeeks(_goal.getNbOfWeek());
        this.daysOfWeek = _daysOfWeek;
        this.longOutgoing = _longOutgoing;
    }

    public TrainingPlan(Account account, Goal goal, LocalDate startDate,
                        List<DayOfWeek> daysOfWeek, List<DayOfWeek> longOutgoing) {
        this(account, List.of(goal), startDate, daysOfWeek, longOutgoing);
    }

    public TrainingPlan(Account account, List<Goal> goals, LocalDate startDate,
                        List<DayOfWeek> daysOfWeek, List<DayOfWeek> longOutgoing) {
        this.account = account;
        this.goals = goals;
        this.startDate = startDate;
        this.endDate = startDate.plusWeeks(goals.stream().findFirst().get().getNbOfWeek());
        this.daysOfWeek = daysOfWeek;
        this.longOutgoing = longOutgoing;
    }

    // GETTERS & SETTERS ----------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public List<Goal> getGoals() { return goals; }
    public void setGoals(List<Goal> goals) { this.goals = goals; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<DayOfWeek> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public List<DayOfWeek> getLongOutgoing() { return longOutgoing; }
    public void setLongOutgoing(List<DayOfWeek> longOutgoing) { this.longOutgoing = longOutgoing; }

    public List<Workout> getWorkouts() { return workouts; }
    public void setWorkouts(List<Workout> workouts) { this.workouts = workouts; }

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