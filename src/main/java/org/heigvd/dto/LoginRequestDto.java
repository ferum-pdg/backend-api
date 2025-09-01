package org.heigvd.dto;

import io.smallrye.common.constraint.NotNull;

public class LoginRequestDto {
    @NotNull
    public String email;

    @NotNull
    public String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

