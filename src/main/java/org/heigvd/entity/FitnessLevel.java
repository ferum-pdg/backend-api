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
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @NotNull
    private LocalDate date;

    @Min(1)
    @Max(10)
    @Column(name = "fitness_score")
    private int fitnessScore;

    // CONSTRUCTORS ---------------------------------------------

    public FitnessLevel() {}

    public FitnessLevel(Account account, LocalDate date, int fitnessScore) {
        this.account = account;
        this.date = date;
        this.fitnessScore = fitnessScore;
    }

    // GETTERS & SETTERS ----------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getFitnessScore() { return fitnessScore; }
    public void setFitnessScore(int fitnessScore) { this.fitnessScore = fitnessScore; }
}
