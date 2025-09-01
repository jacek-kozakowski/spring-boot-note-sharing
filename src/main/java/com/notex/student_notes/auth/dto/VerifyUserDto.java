package com.notex.student_notes.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyUserDto {
    @NotBlank
    @Size(min=3, max=50)
    private String username;
    @NotBlank
    @Size(max=6)
    private String verificationCode;
}
