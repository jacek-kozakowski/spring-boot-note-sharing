package com.notex.student_notes.group.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotBlank
    @Size(max = 5000)
    private String description;

    @NotNull
    @JsonProperty("isPrivate")
    private boolean isPrivate;

    private String password;

}
