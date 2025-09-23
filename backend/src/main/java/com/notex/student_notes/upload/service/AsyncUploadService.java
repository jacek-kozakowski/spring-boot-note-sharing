package com.notex.student_notes.upload.service;

import com.notex.student_notes.minio.service.MinioService;
import com.notex.student_notes.note.model.NoteImage;
import com.notex.student_notes.note.repository.NoteImageRepository;
import com.notex.student_notes.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncUploadService {

    private final MinioService minioService;
    private final NoteImageRepository noteImageRepository;
    private final NoteRepository noteRepository;

    @Async("uploadTaskExecutor")
    public CompletableFuture<List<NoteImage>> uploadFilesAsync(List<MultipartFile> files, Long noteId) {
        log.info("Starting async upload of {} files for note {}", files.size(), noteId);
        
        List<CompletableFuture<NoteImage>> uploadFutures = files.stream()
            .filter(file -> !file.isEmpty())
            .map(file -> uploadSingleFileAsync(file, noteId))
            .toList();
        
        // Wait for all uploads to complete
        CompletableFuture<Void> allUploads = CompletableFuture.allOf(
            uploadFutures.toArray(new CompletableFuture[0])
        );
        
        return allUploads.thenApply(v -> {
            List<NoteImage> uploadedImages = uploadFutures.stream()
                .map(CompletableFuture::join)
                .toList();
            
            // Clear cache for the note
            evictNoteCache(noteId);
            
            log.info("Completed async upload of {} files for note {}", uploadedImages.size(), noteId);
            return uploadedImages;
        });
    }

    @Async("uploadTaskExecutor")
    public CompletableFuture<NoteImage> uploadSingleFileAsync(MultipartFile file, Long noteId) {
        try {
            log.debug("Uploading file: {}", file.getOriginalFilename());
            
            String uploadedFilename = minioService.uploadFile(
                file.getOriginalFilename(),
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
            );

            NoteImage noteImage = new NoteImage();
            noteImage.setNote(noteRepository.findById(noteId).orElseThrow(() -> new RuntimeException("Note not found")));
            noteImage.setFilename(uploadedFilename);
            noteImage.setIndex(getNextIndexForNote(noteId));

            NoteImage savedImage = noteImageRepository.save(noteImage);
            log.debug("Successfully uploaded file: {}", file.getOriginalFilename());
            
            return CompletableFuture.completedFuture(savedImage);
            
        } catch (IOException e) {
            log.error("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
        }
    }

    @Transactional
    private int getNextIndexForNote(Long noteId) {
        Integer maxIndex = noteImageRepository.findMaxIndexByNoteId(noteId);
        return (maxIndex != null) ? maxIndex + 1 : 1;
    }

    @CacheEvict(value = "notes", key = "#noteId")
    public void evictNoteCache(Long noteId) {
        log.debug("Evicting cache for note {}", noteId);
    }
}