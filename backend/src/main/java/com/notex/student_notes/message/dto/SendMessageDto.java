package com.notex.student_notes.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageDto {
    private Long groupId;

    @NotBlank
    @Size(max=500)
    private String content;
}
