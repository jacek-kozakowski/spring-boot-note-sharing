package com.notex.student_notes.note.model;

import com.notex.student_notes.note.dto.CreateNoteDto;
import com.notex.student_notes.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean deleted;
    private LocalDateTime deletedAt;


    public Note(CreateNoteDto input, User owner){
        this.title = input.getTitle();
        this.content = input.getContent();
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
        this.deletedAt = null;
    }

}
