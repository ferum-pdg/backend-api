package org.heigvd.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class FitnessLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private LocalDate date;

    @Min(1)
    @Max(100)
    @Column(name = "fitness_level")
    private Integer fitnessLevel;

    // CONSTRUCTORS ---------------------------------------------

    public FitnessLevel() {
    }

    public FitnessLevel(LocalDate date, int fitnessLevel) {
        this.date = date;
        this.fitnessLevel = fitnessLevel;
    }

    // METHODS --------------------------------------------------

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getFitnessLevel() {
        return fitnessLevel;
    }

    public void setFitnessLevel(int fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
    }

}