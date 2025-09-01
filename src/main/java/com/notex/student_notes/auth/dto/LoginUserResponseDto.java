package com.notex.student_notes.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginUserResponseDto {
    private String token;
    private Long tokenExpirationTime;

    public LoginUserResponseDto(String token, Long tokenExpirationTime) {
        this.token = token;
        this.tokenExpirationTime = tokenExpirationTime;
    }
}
