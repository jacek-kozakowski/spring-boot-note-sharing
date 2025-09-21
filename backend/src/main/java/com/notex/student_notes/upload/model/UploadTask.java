package com.notex.student_notes.upload.model;


import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.user.model.User;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "upload_tasks")
public class UploadTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 500)
    private String filename;
    @NotBlank
    @Column(length = 500)
    private String originalFilename;
    @NotBlank
    private String contentType;
    @NotNull
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Enumerated(EnumType.STRING)
    private UploadStatus status = UploadStatus.PENDING;

    @Column(length = 1000)
    private String errorMessage;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    @Column(length = 1000)
    private String tempFilePath;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastRetryAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    public boolean canRetry() {
        return retryCount < MAX_RETRIES && status == UploadStatus.FAILED;
    }

    public void markAsCompleted() {
        this.status = UploadStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String error) {
        this.status = UploadStatus.FAILED;
        this.errorMessage = error;
    }

    public void markAsProcessing() {
        this.status = UploadStatus.PROCESSING;
    }

    public boolean isPending() {
        return status == UploadStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == UploadStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == UploadStatus.FAILED;
    }
}
