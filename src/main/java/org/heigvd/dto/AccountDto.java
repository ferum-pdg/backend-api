package org.heigvd.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public class AccountDto {

    private UUID id;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phoneNumber;

    private LocalDate birthDate;

    @DecimalMin(value = "0.0", message = "Weight must be positive")
    private Double weight;

    @DecimalMin(value = "0.0", message = "Height must be positive")
    private Double height;

    @Min(value = 1, message = "FCMax must be at least 1")
    @Max(value = 220, message = "FCMax must not exceed 220")
    private Integer fcMax;

    // CONSTRUCTORS ---------------------------------------------

    public AccountDto() {}

    public AccountDto(UUID id, String email, String firstName, String lastName,
                      String phoneNumber, LocalDate birthDate, Double weight,
                      Double height, Integer fcMax) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.weight = weight;
        this.height = height;
        this.fcMax = fcMax;
    }

    // GETTERS & SETTERS ----------------------------------------

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public int getFcMax() {
        return fcMax;
    }

    public void setFcMax(Integer fcMax) {
        this.fcMax = fcMax;
    }
}