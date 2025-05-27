package com.flamedavid.eurovision.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserDTO {
    @NotNull
    @Size(min = 3, max = 20)
    private String username;

    @Email
    private String email;

    @NotNull
    @Size(min = 6, max = 50)
    private String password;

    @NotNull
    private String assignedCountry;
}
