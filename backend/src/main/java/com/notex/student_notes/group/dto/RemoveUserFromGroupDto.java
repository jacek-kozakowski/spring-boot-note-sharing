package com.notex.student_notes.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RemoveUserFromGroupDto {
    @NotBlank
    private String username;
}
