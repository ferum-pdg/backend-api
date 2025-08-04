package org.heigvd.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String email;

    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private Double weight;

    private Double height;

    @Column(name = "fcmax")
    private int FCMax;

    @OneToMany
    private List<FitnessLevel> fitnessLevels  = new ArrayList<>();

    // CONSTRUCTORS ---------------------------------------------

    public Account() {}

    public Account(String email, String password, String firstName, String lastName, String phoneNumber,
                   LocalDate birthDate, Double weight, Double height, int FCMax) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.weight = weight;
        this.height = height;
        this.FCMax = FCMax;
    }

    public Account(String email, String firstName, String lastName,
                   LocalDate birthDate, Double weight, Double height, int FCMax) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.weight = weight;
        this.height = height;
        this.FCMax = FCMax;
    }

    // METHODS --------------------------------------------------

    public List<FitnessLevel> getFitnessLevels() { return fitnessLevels; }
    public void setFitnessLevels(List<FitnessLevel> fitnessLevels) { this.fitnessLevels = fitnessLevels; }
    public void addFitnessLevel(FitnessLevel fitnessLevel) { this.fitnessLevels.add(fitnessLevel); }
    public FitnessLevel getLastFitnessLevel() {
        if (fitnessLevels == null || fitnessLevels.isEmpty()) {
            return null;
        }
        return fitnessLevels.getLast();
    }

}
