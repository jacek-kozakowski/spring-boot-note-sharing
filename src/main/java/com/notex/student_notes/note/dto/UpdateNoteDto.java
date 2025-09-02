package com.notex.student_notes.note.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateNoteDto {
    @Size(min = 3, max = 255)
    private String title;

    private String content;

    public boolean hasTitle(){
        return title != null && !title.isBlank();
    }
    public boolean hasContent(){
        return content != null && !title.isBlank();
    }
    public boolean hasAny(){
        return hasTitle() || hasContent();
    }
}
