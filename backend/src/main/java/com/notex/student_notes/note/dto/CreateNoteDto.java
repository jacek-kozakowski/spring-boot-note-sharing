package com.notex.student_notes.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteDto {
    private final static int MAX_CONTENT_LENGTH = 5000;

    @NotBlank
    @Size(min = 3, max = 255)
    private String title;

    @NotBlank
    @Size(max = MAX_CONTENT_LENGTH)
    private String content;

    private List<MultipartFile> images;
}
