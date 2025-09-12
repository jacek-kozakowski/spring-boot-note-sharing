package com.notex.student_notes.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendVerificationDto {
    @NotBlank
    private String username;
}
