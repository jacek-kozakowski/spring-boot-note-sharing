package com.notex.student_notes.summary.model;

import com.notex.student_notes.note.model.Note;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "summaries")
public class Summary {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false, length = 5000)
    private String text;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.expiresAt = LocalDateTime.now().plusDays(1);
    }
}
