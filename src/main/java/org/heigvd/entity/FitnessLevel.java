package org.heigvd.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class FitnessLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Account account;

    private LocalDate date;

    @Column(name = "fitness_score")
    private int fitnessScore;

    // CONSTRUCTORS ---------------------------------------------


    // METHODS --------------------------------------------------
}
