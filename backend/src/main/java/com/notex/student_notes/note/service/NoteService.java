package com.notex.student_notes.note.service;

import com.notex.student_notes.auth.dto.NoChangesProvidedException;
import com.notex.student_notes.config.metrics.CustomMetrics;
import com.notex.student_notes.minio.service.MinioService;
import com.notex.student_notes.note.dto.CreateNoteDto;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.dto.UpdateNoteDto;
import com.notex.student_notes.note.exceptions.NoteDeletedException;
import com.notex.student_notes.note.exceptions.NoteImageDeleteException;
import com.notex.student_notes.note.exceptions.NoteNotFoundException;
import com.notex.student_notes.note.exceptions.UserNotNoteOwner;
import com.notex.student_notes.note.mapper.NoteMapper;
import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.model.NoteImage;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.upload.service.UploadService;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import io.micrometer.core.instrument.Timer;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteMapper noteMapper;
    private final MinioService minioService;
    private final UploadService uploadService;
    private final CustomMetrics customMetrics;

    private static final Filter FILTER_FOR_USER = Filter.ACTIVE;

    public User getUser(String username){
        return userRepository.findByUsername(username).orElseThrow(()->{
            log.warn("Fail - User {} not found.", username);
            return new UserNotFoundException("User not found");
        });
    }

    public List<NoteDto> getNotesByPartialName(String partialName){
        log.info("Fetching notes by partial name {}", partialName);
        List<Note>  notes = noteRepository.findAllByTitleContainingIgnoreCase(partialName);
        log.debug("Success - Fetched {} notes by partial name {}", notes.size(), partialName);
        return convertToNoteDto(notes, FILTER_FOR_USER);
    }
    
    public List<NoteDto> getUsersNotes(User user){
        log.info("User {} fetching their notes", user.getUsername());
        List<NoteDto> userNotes = convertToNoteDto(noteRepository.findAllByOwner(user), FILTER_FOR_USER);
        log.debug("User {} fetched {} notes", user.getUsername(), userNotes.size());
        return userNotes;
    }

    public List<NoteDto> getUsersNotes(String username){
        log.info("Fetching {}'s notes", username);
        User user = getUser(username);
        List<NoteDto> userNotes = convertToNoteDto(noteRepository.findAllByOwner(user), FILTER_FOR_USER);
        log.debug("Fetched {} {}'s notes", userNotes.size(), username);
        return userNotes;
    }

    public List<NoteDto> getUsersNotesAdmin(String username, Filter filter){
        log.info("Admin fetching users {} notes",username);
        User user = getUser(username);
        List<NoteDto> userNotes = convertToNoteDto(noteRepository.findAllByOwner(user), filter);
        log.debug("Success - Admin fetched {} notes", userNotes.size());
        return userNotes;
    }


    // Cache disabled for getNoteById to ensure fresh data with images
    // @Cacheable(value = "notes", key = "#id")
    public NoteDto getNoteById(Long id){
        log.info("Fetching note {}", id);
        NoteDto note = noteMapper.toDto(findNoteById(id));
        log.debug("Success - Fetched note {}", id);
        return note;
    }

    @Transactional
    public NoteDto createNote(CreateNoteDto inputNote, User owner){
        Timer.Sample sample = customMetrics.startNoteProcessingTimer();
        log.info("User {} creating a note.", owner.getUsername());
        Note noteToCreate = new Note(inputNote, owner);
        Note createdNote = noteRepository.save(noteToCreate);

        if (inputNote.getImages() != null && !inputNote.getImages().isEmpty()){
            log.info("Queuing {} images for async upload to note {}", inputNote.getImages().size(), createdNote.getId());
            for (MultipartFile file : inputNote.getImages()) {
                if (!file.isEmpty()) {
                    try {
                        uploadService.queueUpload(createdNote.getId(), file, owner);
                        log.debug("Queued image {} for upload", file.getOriginalFilename());
                    } catch (Exception e) {
                        log.error("Failed to queue image {} for upload: {}", file.getOriginalFilename(), e.getMessage());
                    }
                }
            }
        }

        log.debug("Success - User {} created note {} with {} images queued for upload",
                owner.getUsername(), createdNote.getId(),
                inputNote.getImages() != null ? inputNote.getImages().size() : 0);
        sample.stop(customMetrics.getNoteProcessingTimer());
        customMetrics.incrementNoteCreatedCounter();
        return noteMapper.toDto(createdNote);
    }

    @CachePut(value = "notes", key = "#id")
    @Transactional
    public NoteDto updateNote(Long id, UpdateNoteDto inputNote, User currentUser){
        log.info("Updating note {}", id);
        if(!verifyUserIsOwner(id, currentUser)){
            log.warn("Fail - User {} can't update note {}: User is not the owner.", currentUser.getUsername(), id);
            throw new UserNotNoteOwner("User is not the owner of the note.");
        }
        if (!inputNote.hasAny()){
            log.warn("Fail - Can't update note {}: request is empty.", id);
            throw new NoChangesProvidedException("Empty request. Can't update note.");
        }
        Note noteToUpdate = findNoteById(id);
        if (noteToUpdate.isDeleted()){
            log.warn("Fail - Note {} is deleted", id);
            throw new NoteDeletedException("Note was deleted");
        }
        if (inputNote.hasTitle()){
            noteToUpdate.setTitle(inputNote.getTitle());
        }
        if (inputNote.hasContent()){
            noteToUpdate.setContent(inputNote.getContent());
        }
        if (inputNote.hasRemoveImages()){
            for (Integer imageIndex : inputNote.getRemoveImageIndexes()){
                NoteImage noteImage = noteToUpdate.getImages().stream()
                        .filter(i -> i.getIndex() == imageIndex)
                        .findFirst().orElse(null);
                if (noteImage != null){
                    try {
                        removeNoteImage(noteImage, noteToUpdate);
                    }catch (Exception e){
                        log.error("Error - Failed to delete note image from MinIO", e);
                        throw new NoteImageDeleteException("Failed to delete note image from MinIO");
                    }
                    log.debug("Success - Note images {} removed.", inputNote.getRemoveImageIndexes());
                }
            }
        }
        if (inputNote.hasImages()){
            log.info("Queuing {} new images for async upload to note {}", inputNote.getNewImages().size(), id);
            User noteOwner = noteToUpdate.getOwner();

            for (MultipartFile file : inputNote.getNewImages()) {
                if (!file.isEmpty()) {
                    try {
                        uploadService.queueUpload(id, file, noteOwner);
                        log.debug("Queued new image {} for upload to note {}", file.getOriginalFilename(), id);
                    } catch (Exception e) {
                        log.error("Failed to queue image {} for upload: {}", file.getOriginalFilename(), e.getMessage());
                    }
                }
            }
            log.debug("Success - Queued {} new images for async upload.", inputNote.getNewImages().size());
        }
        noteToUpdate.setUpdatedAt(LocalDateTime.now());
        normalizeImageIndexes(noteToUpdate);
        Note updatedNote = noteRepository.save(noteToUpdate);
        NoteDto updatedNoteDto = noteMapper.toDto(updatedNote);
        log.debug("Success - note {} updated.", id);
        customMetrics.incrementNoteUpdatedCounter();
        return updatedNoteDto;
    }

    @CacheEvict(value = "notes", key = "#id")
    @Transactional
    public void deleteNote(Long id, User currentUser) {
        log.info("Deleting note {}", id );
        if(!verifyUserIsOwner(id, currentUser)){
            log.warn("Fail - User {} can't delete note {}: User is not the owner.", currentUser.getUsername(), id);
            throw new UserNotNoteOwner("User is not the owner of the note.");
        }
        Note noteToDelete = findNoteById(id);
        noteToDelete.setDeleted(true);
        noteToDelete.setDeletedAt(LocalDateTime.now());
        try {
            for (NoteImage noteImage : new ArrayList<>(noteToDelete.getImages())) {
                removeNoteImage(noteImage, noteToDelete);
            }
        }catch (Exception e){
            log.error("Error - Failed to delete note images from MinIO", e);
            throw new NoteImageDeleteException("Failed to delete note images from MinIO");
        }
        noteRepository.save(noteToDelete);
        log.debug("Success - Note {} deleted.", id);
        customMetrics.incrementNoteDeletedCounter();
    }

    @CacheEvict(value = "notes", key = "#noteId")
    @Transactional
    public void deleteNoteImage(Long noteId, Long imageId, User currentUser) {
        log.info("Deleting note image {} from note {}", imageId, noteId);
        if(!verifyUserIsOwner(noteId, currentUser)){
            log.warn("Fail - User {} can't delete note image {}: User is not the owner.", currentUser.getUsername(), imageId);
            throw new UserNotNoteOwner("User is not the owner of the note.");
        }
        Note note = findNoteById(noteId);
        NoteImage noteImage = note.getImages().stream()
                .filter(i -> Objects.equals(i.getId(), imageId))
                .findFirst().orElse(null);
        if (noteImage != null){
            try {
                removeNoteImage(noteImage, note);
            }catch (Exception e){
                log.error("Error - Failed to delete note image from MinIO", e);
                throw new NoteImageDeleteException("Failed to delete note image from MinIO");
            }
            normalizeImageIndexes(note);
            noteRepository.save(note);
            log.debug("Success - Note image {} deleted from note {}", imageId, noteId);
        }
    }

    private boolean verifyUserIsOwner(Long id, User user){
        return noteRepository.existsByIdAndOwnerId(id, user.getId());
    }

    private List<NoteDto> convertToNoteDto(List<Note> notes, Filter filter){
        return notes.stream()
                .filter(note -> switch (filter) {
                    case ALL -> true;
                    case DELETED -> note.isDeleted();
                    case ACTIVE -> !note.isDeleted();
                })
                .map(noteMapper::toDto)
                .toList();
    }
    
    private Note findNoteById(Long id){
        Note note = noteRepository.findById(id).orElseThrow(()->{
            log.warn("Fail - Note {} does not exist.", id);
            return new NoteNotFoundException("Note not found");
        });
        if (note.isDeleted()){
            log.warn("Fail - Note {} is deleted", id);
            throw new NoteDeletedException("Note was deleted");
        }
        return note;
    }

    private void removeNoteImage(NoteImage noteImage, Note noteToUpdate) throws Exception {
        minioService.deleteFile(noteImage.getFilename());
        noteToUpdate.removeImage(noteImage);
    }


    private void normalizeImageIndexes(Note note){
        List<NoteImage> images = note.getImages()
                .stream()
                .sorted(Comparator.comparingInt(NoteImage::getIndex))
                .toList();

        for (int i = 0; i < images.size(); i++) {
            images.get(i).setIndex(i);
        }
    }
}
