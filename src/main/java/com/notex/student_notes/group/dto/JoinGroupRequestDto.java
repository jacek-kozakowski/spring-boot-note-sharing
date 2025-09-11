package com.notex.student_notes.group.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JoinGroupRequestDto {
    @Size(min=8)
    private String password;
}
