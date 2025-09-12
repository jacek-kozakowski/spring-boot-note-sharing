package com.notex.student_notes.upload.repository;

import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.upload.model.UploadStatus;
import com.notex.student_notes.upload.model.UploadTask;
import com.notex.student_notes.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UploadTaskRepository extends JpaRepository<UploadTask, Long> {
    List<UploadTask> findAllByStatus(UploadStatus status);

    List<UploadTask> findAllByUserAndStatus(User user, UploadStatus status);

    @Query("SELECT t FROM UploadTask t WHERE t.status = 'FAILED' AND t.retryCount < 3 AND (t.lastRetryAt IS NULL OR t.lastRetryAt < :retryAfter)")
    List<UploadTask> findFailedTasksForRetry(@Param("retryAfter") LocalDateTime retryAfter);

    @Query("SELECT t FROM UploadTask t WHERE t.user = :user ORDER BY t.createdAt DESC")
    List<UploadTask> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    List<UploadTask> findAllByNote(Note note);

    @Query("SELECT COUNT(t) FROM UploadTask t WHERE t.user = :user AND t.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") UploadStatus status);

    @Query("SELECT t FROM UploadTask t WHERE t.status = 'PENDING' ORDER BY t.createdAt ASC")
    List<UploadTask> findPendingTasksOrderByCreatedAt();
}
