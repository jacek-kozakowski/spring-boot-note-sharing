package com.notex.student_notes.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {
    @NotBlank
    @Size(min=3, max=50)
    private String username;

    @NotBlank
    @Size(min = 8)
    private String password;

    @Email
    private String email;

    @Size(max=100)
    @NotBlank
    private String firstName;

    @Size(max=100)
    @NotBlank
    private String lastName;
}
