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

    @Column(name = "nb_of_workouts_per_week")
    private int nbOfWorkoutsPerWeek;

    @Column(name = "target_distance")
    private Double targetDistance;

    @Column(name = "elevation_gain")
    private Double elevetionGain;
}
