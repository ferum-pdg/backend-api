package org.heigvd.entity;

import java.time.LocalDate;
import java.util.UUID;

public class User {
    UUID id;
    String email;
    String password;
    String firstName;
    String lastName;
    String phoneNumber;
    LocalDate birthDate;
    Double weight;
    Double height;
}
