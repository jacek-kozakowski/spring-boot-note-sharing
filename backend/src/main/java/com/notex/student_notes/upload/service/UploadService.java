package com.notex.student_notes.upload.service;

import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final NoteRepository noteRepository;
    private final AsyncUploadService asyncUploadService;

    public CompletableFuture<List<com.notex.student_notes.note.model.NoteImage>> queueUpload(Long noteId, List<MultipartFile> files, User user) {
        log.info("Queuing upload of {} files for note {} by user {}", files.size(), noteId, user.getUsername());

        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found"));

        return asyncUploadService.uploadFilesAsync(files, noteId);
    }

    public CompletableFuture<com.notex.student_notes.note.model.NoteImage> queueUpload(Long noteId, MultipartFile file, User user) {
        log.info("Queuing upload of file {} for note {} by user {}", file.getOriginalFilename(), noteId, user.getUsername());

        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found"));

        return asyncUploadService.uploadSingleFileAsync(file, noteId);
    }
}