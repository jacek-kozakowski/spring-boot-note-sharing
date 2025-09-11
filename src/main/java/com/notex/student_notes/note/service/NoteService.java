package com.notex.student_notes.note.service;

import com.notex.student_notes.auth.dto.NoChangesProvidedException;
import com.notex.student_notes.minio.service.MinioService;
import com.notex.student_notes.note.dto.CreateNoteDto;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.dto.UpdateNoteDto;
import com.notex.student_notes.note.exceptions.NoteDeletedException;
import com.notex.student_notes.note.exceptions.NoteImageDeleteException;
import com.notex.student_notes.note.exceptions.NoteImageUploadException;
import com.notex.student_notes.note.exceptions.NoteNotFoundException;
import com.notex.student_notes.note.mapper.NoteMapper;
import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.model.NoteImage;
import com.notex.student_notes.note.repository.NoteImageRepository;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteImageRepository noteImageRepository;
    private final NoteMapper noteMapper;

    private static final String FILTER_FOR_USER = "active";
    private final MinioService minioService;


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

    public List<NoteDto> getUsersNotesAdmin(String username, String filter){
        log.info("Admin fetching users {} notes",username);
        User user = getUser(username);
        List<NoteDto> userNotes = convertToNoteDto(noteRepository.findAllByOwner(user), filter);
        log.debug("Success - Admin fetched {} notes", userNotes.size());
        return userNotes;
    }
    public NoteDto getNoteById(Long id){
        log.info("Fetching note {}", id);
        NoteDto note = noteMapper.toDto(findNoteById(id));
        log.debug("Success - Fetched note {}", id);
        return note;
    }

    @Transactional
    public NoteDto createNote(CreateNoteDto inputNote, User owner){
        log.info("User {} creating a note.", owner.getUsername());
        Note createdNote = new Note(inputNote, owner);

        if (inputNote.getImages() != null){
            for (int i = 0; i < inputNote.getImages().size(); i++) {
                MultipartFile file = inputNote.getImages().get(i);
                try{
                    addNoteImage(createdNote, i, file);
                }catch (Exception e){
                    log.error("Error - Failed to upload image to MinIO", e);
                    throw new NoteImageUploadException("Failed to upload image to MinIO");
                }
            }

        }
        Note savedNote = noteRepository.save(createdNote);
        log.debug("Success - User {} created a note.", owner.getUsername());
        return noteMapper.toDto(savedNote);
    }


    @Transactional
    public NoteDto updateNote(Long id, UpdateNoteDto inputNote){
        log.info("Updating note {}", id);
        if (!inputNote.hasAny()){
            log.warn("Fail - Can't update note {}: request is empty.", id);
            throw new NoChangesProvidedException("Empty request. Can't update note.");
        }
        Note noteToUpdate = findNoteById(id);
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
            int currentMaxIndex = noteToUpdate.getImages().stream()
                    .mapToInt(NoteImage::getIndex)
                    .max()
                    .orElse(-1);

            for (int i = 0; i < inputNote.getNewImages().size(); i++) {
                int currIndex = i + currentMaxIndex + 1;
                MultipartFile file = inputNote.getNewImages().get(i);
                try{
                    addNoteImage(noteToUpdate, currIndex, file);
                }catch (Exception e){
                    log.error("Error - Failed to upload image to MinIO", e);
                    throw new RuntimeException("Failed to upload image to MinIO");
                }
            }
            log.debug("Success - Added {} note images.", inputNote.getNewImages().size());
            
        }
        noteToUpdate.setUpdatedAt(LocalDateTime.now());
        normalizeImageIndexes(noteToUpdate);
        Note updatedNote = noteRepository.save(noteToUpdate);
        NoteDto updatedNoteDto = noteMapper.toDto(updatedNote);
        log.debug("Success - note {} updated.", id);
        return updatedNoteDto;
    }

    @Transactional
    public void deleteNote(Long id) {
        log.info("Deleting note {}", id );
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
    }

    @Transactional
    public void deleteNoteImage(Long noteId, Long imageId) {
        log.info("Deleting note image {} from note {}", imageId, noteId);
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

    public boolean verifyUserIsOwner(Long id, User user){
        Note note = findNoteById(id);
        return note.getOwner().equals(user);
    }

    private List<NoteDto> convertToNoteDto(List<Note> notes, String filter){
        return notes.stream()
                .filter(note -> switch (filter.toLowerCase()) {
                    case "all" -> true;
                    case "deleted" -> note.isDeleted();
                    case "active" -> !note.isDeleted();
                    default -> throw new IllegalArgumentException("Unknown filter: " + filter);
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

    private void addNoteImage(Note note, int i, MultipartFile file) throws Exception {
        String filename = note.getId() + "_" + i +  "." + file.getOriginalFilename();
        minioService.uploadFile(filename, file.getInputStream(), file.getSize(), file.getContentType());
        NoteImage noteImage = new NoteImage();
        noteImage.setIndex(i);
        noteImage.setNote(note);
        noteImage.setFilename(filename);
        note.addImage(noteImage);
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
