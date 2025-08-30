package org.heigvd.entity.workout.details;

import jakarta.persistence.*;
import org.heigvd.dto.workout_dto.WorkoutPlanDetailsDto;
import org.heigvd.dto.workout_dto.WorkoutPlanDto;
import org.heigvd.entity.workout.WorkoutType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bloc_id", nullable = false)
    private int blocId;

    @Column(name = "repetition_count", nullable = false)
    private int repetitionCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutPlanDetails> details = new ArrayList<>();

    private WorkoutType type;

    // CONSTRUCTORS ----------------------------------------------------------------------------------------------------

    public WorkoutPlan() {}

    public WorkoutPlan(int blocId, int repetitionCount, List<WorkoutPlanDetails> details, WorkoutType type) {
        this.blocId = blocId;
        this.repetitionCount = repetitionCount;
        this.details = details;
        this.type = type;
    }

    // METHODS ---------------------------------------------------------------------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public int getBlocId() { return blocId; }
    public void setBlocId(int blocId) { this.blocId = blocId; }

    public int getRepetitionCount() { return repetitionCount; }
    public void setRepetitionCount(int repetitionCount) { this.repetitionCount = repetitionCount; }

    public List<WorkoutPlanDetails> getDetails() { return details; }
    public void setDetails(List<WorkoutPlanDetails> details) { this.details = details; }

    public WorkoutType getType() { return type; }
    public void setWorkoutType(WorkoutType type) { this.type = type; }

    @Override
    public String toString() {
        return "\n{" + "\n" +
                " id=" + id + "\n" +
                " blocId=" + blocId + "\n" +
                " repetitionCount=" + repetitionCount + "\n" +
                " details=" + details + "\n" +
                " type=" + type + "\n" +
                '}';
    }

}
