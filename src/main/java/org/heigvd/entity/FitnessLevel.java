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
    @JoinColumn(name = "account_id", nullable = true)
    private Account account;

    private LocalDate date;

    @Column(name = "fitness_score")
    private int fitnessScore;

    // CONSTRUCTORS ---------------------------------------------
    public FitnessLevel() {}

    public FitnessLevel(Account account, LocalDate date, int fitnessScore) {
        this.account = account;
        this.date = date;
        this.fitnessScore = fitnessScore;
    }

    // GETTERS AND SETTERS --------------------------------------

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getFitnessScore() {
        return fitnessScore;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setFitnessScore(int fitnessScore) {
        this.fitnessScore = fitnessScore;
    }
}
