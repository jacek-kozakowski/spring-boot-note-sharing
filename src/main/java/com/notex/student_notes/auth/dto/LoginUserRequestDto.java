package com.notex.student_notes.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserRequestDto {
    @NotBlank
    @Size(min=3, max=50)
    private String username;

    @NotBlank
    @Size(min = 8)
    private String password;
}
