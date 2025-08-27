package org.heigvd.entity.workout.details;

import jakarta.persistence.*;
import org.heigvd.entity.workout.IntensityZone;

import java.util.UUID;

@Entity
public class WorkoutPlanDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bloc_detail_id", nullable = false)
    private int blocDetailId;

    @Column(name = "duration_sec", nullable = false)
    private int durationSec;

    @Enumerated(EnumType.STRING)
    @Column(name = "intensity_zone", nullable = false)
    private IntensityZone intensityZone;

    // CONSTRUCTORS ----------------------------------------------------------------------------------------------------¨

    public WorkoutPlanDetails() {}

    public WorkoutPlanDetails(int blocDetailId, int durationSec, IntensityZone intensityZone) {
        this.blocDetailId = blocDetailId;
        this.durationSec = durationSec;
        this.intensityZone = intensityZone;
    }

    // METHODS ---------------------------------------------------------------------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public int getBlocDetailId() { return blocDetailId; }
    public void setBlocDetailId(int blocDetailId) { this.blocDetailId = blocDetailId; }

    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }

    public IntensityZone getIntensityZone() { return intensityZone; }
    public void setIntensityZone(IntensityZone intensityZone) { this.intensityZone = intensityZone; }

    @Override
    public String toString() {
        return "\n {" +  "\n" +
                "  id=" + id +  "\n" +
                "  blocDetailId='" + blocDetailId + "'" +  "\n" +
                "  durationSec='" + durationSec + "'" +  "\n" +
                "  intensityZone='" + intensityZone + "'" +  "\n" +
                " }";
    }

}
