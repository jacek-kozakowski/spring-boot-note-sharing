package com.notex.student_notes.note.dto;

import com.notex.student_notes.note.model.Note;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NoteDto {
    private Long id;
    private String title;
    private String content;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<NoteImageDto> images;

    public NoteDto(Note note) {
        this.id = note.getId();
        this.title = note.getTitle();
        this.content = note.getContent();
        this.ownerUsername = note.getOwner().getUsername();
        this.createdAt = note.getCreatedAt();
        this.updatedAt = note.getUpdatedAt();
    }
}
