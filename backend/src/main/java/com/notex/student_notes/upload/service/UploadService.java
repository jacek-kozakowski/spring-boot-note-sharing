package com.notex.student_notes.upload.service;

import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.upload.model.UploadTask;
import com.notex.student_notes.upload.repository.UploadTaskRepository;
import com.notex.student_notes.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final UploadTaskRepository uploadTaskRepository;
    private final NoteRepository noteRepository;
    private final AsyncUploadService asyncUploadService;

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/notex-uploads/";

    public Long queueUpload(Long noteId, MultipartFile file, User user) throws IOException {
        log.info("Queuing upload for note {} by user {}", noteId, user.getUsername());

        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new RuntimeException("Note not found"));

        Path tempDirPath = Paths.get(TEMP_DIR);
        if (!Files.exists(tempDirPath)) {
            Files.createDirectories(tempDirPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        Path tempFilePath = tempDirPath.resolve(uniqueFilename);
        file.transferTo(tempFilePath.toFile());

        UploadTask task = new UploadTask();
        task.setFilename(uniqueFilename);
        task.setOriginalFilename(originalFilename);
        task.setContentType(file.getContentType());
        task.setFileSize(file.getSize());
        task.setUser(user);
        task.setNote(note);
        task.setTempFilePath(tempFilePath.toString());

        task = uploadTaskRepository.save(task);

        asyncUploadService.processUpload(task.getId(), tempFilePath.toFile());

        log.info("Upload task {} queued successfully", task.getId());
        return task.getId();
    }
}
