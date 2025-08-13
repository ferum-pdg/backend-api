package org.heigvd.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

@Entity
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Sport sport;

    @NotBlank
    private String name;

    @Min(1)
    @Max(14)
    @Column(name = "nb_of_workouts_per_week")
    private int nbOfWorkoutsPerWeek;

    @Min(1)
    @Max(52)
    @Column(name = "nb_of_week")
    private int nbOfWeek;

    @DecimalMin("0.0")
    @Column(name = "target_distance")
    private Double targetDistance;

    @DecimalMin("0.0")
    @Column(name = "elevation_gain")
    private Double elevetionGain;

    // CONSTRUCTORS ---------------------------------------------

    public Goal() {}

    public Goal(Sport sport, String name, int nbOfWorkoutsPerWeek, int nbOfWeek,
                Double targetDistance, Double elevationGain) {
        this.sport = sport;
        this.name = name;
        this.nbOfWorkoutsPerWeek = nbOfWorkoutsPerWeek;
        this.nbOfWeek = nbOfWeek;
        this.targetDistance = targetDistance;
        this.elevetionGain = elevationGain;
    }

    // METHODS --------------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Sport getSport() { return sport; }
    public void setSport(Sport sport) { this.sport = sport; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getNbOfWorkoutsPerWeek() { return nbOfWorkoutsPerWeek; }
    public void setNbOfWorkoutsPerWeek(int nbOfWorkoutsPerWeek) { this.nbOfWorkoutsPerWeek = nbOfWorkoutsPerWeek; }

    public int getNbOfWeek() { return nbOfWeek; }
    public void setNbOfWeek(int nbOfWeek) { this.nbOfWeek = nbOfWeek; }

    public Double getTargetDistance() { return targetDistance; }
    public void setTargetDistance(Double targetDistance) { this.targetDistance = targetDistance; }

    public Double getElevationGain() { return elevetionGain; }
    public void setElevationGain(Double elevationGain) { this.elevetionGain = elevationGain; }

    @Override
    public String toString() {
        return "\n {\n" +
                "  id=" + id + "\n" +
                "  sport=" + sport + "\n" +
                "  name='" + name + '\'' + "\n" +
                "  nbOfWorkoutsPerWeek=" + nbOfWorkoutsPerWeek + "\n" +
                "  nbOfWeek=" + nbOfWeek + "\n" +
                "  targetDistance=" + targetDistance + "\n" +
                "  elevationGain=" + elevetionGain + "\n" +
                " }";
    }
}
