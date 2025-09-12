package com.notex.student_notes.note.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "note_images",
    uniqueConstraints = @UniqueConstraint(columnNames = {"note_id", "index"})
)
@NoArgsConstructor
public class NoteImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Column(nullable = false)
    private String filename;

    @Transient
    private String url;

    @Column(nullable = false)
    private int index;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
