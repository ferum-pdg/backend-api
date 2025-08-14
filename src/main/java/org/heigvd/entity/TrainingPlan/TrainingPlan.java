package org.heigvd.entity.TrainingPlan;

import jakarta.persistence.*;
import org.heigvd.entity.Account;
import org.heigvd.entity.Goal;
import org.heigvd.entity.Sport;
import org.heigvd.entity.Workout.TrainingPlanPhase;
import org.heigvd.entity.Workout.Workout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    @OneToMany(cascade = CascadeType.ALL)
    private List<Workout> workouts;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyPlan> pairWeeklyPlans;

    // temporary field for the training generator
    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase")
    private TrainingPlanPhase currentPhase;

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

    public List<Workout> getWorkouts() { return workouts; }
    public void setWorkouts(List<Workout> workouts) { this.workouts = workouts; }

    public List<DailyPlan> getPairWeeklyPlans() { return pairWeeklyPlans; }
    public void setPairWeeklyPlans(List<DailyPlan> pairWeeklyPlans) { this.pairWeeklyPlans = pairWeeklyPlans; }
    public void addPairWeeklyPlan(DailyPlan dailyPlan) {
        if (this.pairWeeklyPlans == null) {
            this.pairWeeklyPlans = new ArrayList<>();
        }
        this.pairWeeklyPlans.add(dailyPlan);
    }

    public TrainingPlanPhase getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(TrainingPlanPhase currentPhase) { this.currentPhase = currentPhase; }

    @Override
    public String toString() {
        return "TrainingPlan { \n" +
                " id=" + id + " \n" +
                " goals=" + goals + " \n" +
                " startDate=" + startDate + " \n" +
                " endDate=" + endDate + " \n" +
                " daysOfWeek=" + daysOfWeek + " \n" +
                " longOutgoing=" + longOutgoing + " \n" +
                " account=" + account.getEmail() + " \n" +
                " dailyPlans=" + pairWeeklyPlans + " \n" +
                " workouts=" + workouts + " \n" +
                "}";
    }
}