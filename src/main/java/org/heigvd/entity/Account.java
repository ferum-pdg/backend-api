package org.heigvd.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    @Column(name = "first_name")
    private String firstName;

    @NotBlank
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @DecimalMin("0.0")
    private Double weight;

    @DecimalMin("0.0")
    private Double height;

    @Min(1)
    @Max(220)
    @Column(name = "fcmax")
    private int FCMax;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<FitnessLevel> fitnessLevels;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Workout> workouts;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<TrainingPlan> trainingPlans;

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

    // GETTERS & SETTERS ----------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public int getFCMax() { return FCMax; }
    public void setFCMax(int FCMax) { this.FCMax = FCMax; }

    public List<FitnessLevel> getFitnessLevels() { return fitnessLevels; }
    public void setFitnessLevels(List<FitnessLevel> fitnessLevels) { this.fitnessLevels = fitnessLevels; }

    public List<Workout> getWorkouts() { return workouts; }
    public void setWorkouts(List<Workout> workouts) { this.workouts = workouts; }

    public List<TrainingPlan> getTrainingPlans() { return trainingPlans; }
    public void setTrainingPlans(List<TrainingPlan> trainingPlans) { this.trainingPlans = trainingPlans; }
}