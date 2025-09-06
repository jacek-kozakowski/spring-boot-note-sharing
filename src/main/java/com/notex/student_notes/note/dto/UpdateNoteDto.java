package com.notex.student_notes.note.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateNoteDto {
    private final static int MAX_CONTENT_LENGTH = 5000;
    @Size(min = 3, max = 255)
    private String title;

    @Size(max = MAX_CONTENT_LENGTH)
    private String content;

    public boolean hasTitle(){
        return title != null && !title.isBlank();
    }
    public boolean hasContent(){
        return content != null && !content.isBlank();
    }
    public boolean hasAny(){
        return hasTitle() || hasContent();
    }
}
