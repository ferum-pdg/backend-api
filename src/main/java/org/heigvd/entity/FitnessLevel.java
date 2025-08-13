package org.heigvd.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class FitnessLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate date;

    // The fitness score is a value between 0 and 100, where 0 is the worst fitness level and 100 is the best.
    @Column(name = "fitness_score")
    private Integer fitnessLevel;

    // CONSTRUCTORS ---------------------------------------------

    public FitnessLevel() {}

    public FitnessLevel(LocalDate date, int fitnessLevel) {
        this.id = UUID.randomUUID();
        this.date = date;
        this.fitnessLevel = fitnessLevel;
    }

    // METHODS --------------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(int fitnessLevel) { this.fitnessLevel = fitnessLevel; }
}
