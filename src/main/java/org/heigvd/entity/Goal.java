package org.heigvd.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Sport sport;

    private String name;

    @Column(name = "nb_of_workouts_per_week")
    private int nbOfWorkoutsPerWeek;

    @Column(name = "nb_of_week")
    private int nbOfWeek;

    @Column(name = "target_distance")
    private Double targetDistance;

    @Column(name = "elevation_gain")
    private Double elevetionGain;

    // CONSTRUCTORS ---------------------------------------------


    // METHODS --------------------------------------------------
    public UUID getId() { return this.id; }

    public int getNbOfWeek() { return this.nbOfWeek; }

    public Sport getSport() { return this.sport; }

    public int getNbOfWorkoutsPerWeek() { return this.nbOfWorkoutsPerWeek; }

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
