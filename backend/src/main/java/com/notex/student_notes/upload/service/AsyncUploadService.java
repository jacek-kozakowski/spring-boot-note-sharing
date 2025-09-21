package com.notex.student_notes.upload.service;


import com.notex.student_notes.minio.service.MinioService;
import com.notex.student_notes.note.model.NoteImage;
import com.notex.student_notes.note.repository.NoteImageRepository;
import com.notex.student_notes.upload.model.UploadStatus;
import com.notex.student_notes.upload.model.UploadTask;
import com.notex.student_notes.upload.repository.UploadTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncUploadService {

    private final UploadTaskRepository uploadTaskRepository;
    private final MinioService minioService;
    private final NoteImageRepository noteImageRepository;
    
    // Synchronization map to prevent race conditions for index calculation
    private final Map<Long, Object> noteLocks = new ConcurrentHashMap<>();

    @Async("uploadTaskExecutor")
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void processUpload(Long taskId, File tempFile) {
        log.info("Processing upload task {}", taskId);

        UploadTask task = uploadTaskRepository.findById(taskId).orElseThrow(() -> {
            log.warn("Fail - task with {} not found", taskId);
            return new RuntimeException("Task not found");
        });

        if (!task.isPending()) {
            log.warn("Fail - task {} is not pending", taskId);
            return;
        }

        try {
            task.markAsProcessing();
            uploadTaskRepository.save(task);

            try (InputStream inputStream = new FileInputStream(tempFile)) {
                String uploadedFilename = minioService.uploadFile(
                    task.getFilename(),
                    inputStream,
                    task.getFileSize(),
                    task.getContentType()
                );

                NoteImage noteImage = new NoteImage();
                noteImage.setNote(task.getNote());
                noteImage.setFilename(uploadedFilename);
                // Use timestamp as unique index to avoid race conditions
                noteImage.setIndex((int) System.currentTimeMillis());

                noteImageRepository.save(noteImage);

                // Clear cache for the note to ensure fresh data with images
                evictNoteCache(task.getNote().getId());

                log.info("Successfully uploaded file {} for task {}", uploadedFilename, taskId);
            }

            task.markAsCompleted();
            uploadTaskRepository.save(task);

            if (tempFile.exists() && !tempFile.delete()) {
                log.warn("Failed to delete temp file: {}", tempFile.getAbsolutePath());
            }

        } catch (Exception e) {
            log.error("Upload failed for task {}: {}", taskId, e.getMessage());
            task.markAsFailed(e.getMessage());
            task.incrementRetryCount();
            uploadTaskRepository.save(task);

            throw new RuntimeException("Upload failed for task " + taskId, e);
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void retryFailedUploads() {
        LocalDateTime retryAfter = LocalDateTime.now().minusMinutes(5);
        List<UploadTask> failedTasks = uploadTaskRepository.findFailedTasksForRetry(retryAfter);

        for (UploadTask task : failedTasks) {
            if (task.canRetry()) {
                log.info("Retrying failed upload task: {}", task.getId());
                task.setStatus(UploadStatus.PENDING);
                uploadTaskRepository.save(task);

                if (task.getTempFilePath() != null) {
                    File tempFile = new File(task.getTempFilePath());
                    if (tempFile.exists()) {
                        processUpload(task.getId(), tempFile);
                        log.info("Retrying upload for task {} with temp file {}", task.getId(), task.getTempFilePath());
                    } else {
                        log.warn("Temp file {} not found for task {}, marking as failed", task.getTempFilePath(), task.getId());
                        task.markAsFailed("Temp file not found for retry");
                        uploadTaskRepository.save(task);
                    }
                } else {
                    log.warn("No temp file path stored for task {}, marking as failed", task.getId());
                    task.markAsFailed("No temp file path stored");
                    uploadTaskRepository.save(task);
                }
            }
        }
    }

    @CacheEvict(value = "notes", key = "#noteId")
    public void evictNoteCache(Long noteId) {
        log.debug("Evicting cache for note {}", noteId);
    }


    @Transactional
    private int getNextIndexForNoteSync(Long noteId) {
        // Use synchronized block to prevent race conditions
        Object lock = noteLocks.computeIfAbsent(noteId, k -> new Object());
        
        synchronized (lock) {
            try {
                return noteImageRepository.findByNote_IdOrderByIndexDesc(noteId)
                    .stream()
                    .findFirst()
                    .map(img -> img.getIndex() + 1)
                    .orElse(0);
            } finally {
                // Clean up lock after use to prevent memory leaks
                noteLocks.remove(noteId, lock);
            }
        }
    }
}
