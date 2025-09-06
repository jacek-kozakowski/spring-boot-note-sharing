package com.notex.student_notes.note.service;

import com.notex.student_notes.auth.dto.NoChangesProvidedException;
import com.notex.student_notes.note.dto.CreateNoteDto;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.dto.UpdateNoteDto;
import com.notex.student_notes.note.exceptions.NoteDeletedException;
import com.notex.student_notes.note.exceptions.NoteNotFoundException;
import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    private static final String FILTER_FOR_USER = "active";


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
        NoteDto note = new NoteDto(findNoteById(id));
        log.debug("Success - Fetched note {}", id);
        return note;
    }

    public NoteDto createNote(CreateNoteDto inputNote, User owner){
        log.info("User {} creating a note.", owner.getUsername());
        Note createdNote = new Note(inputNote, owner);
        NoteDto savedNote = new NoteDto(noteRepository.save(createdNote));
        log.debug("Success - User {} created a note.", owner.getUsername());
        return savedNote;
    }

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
        noteToUpdate.setUpdatedAt(LocalDateTime.now());
        NoteDto updatedNote = new NoteDto(noteRepository.save(noteToUpdate));
        log.debug("Success - note {} updated.", id);
        return updatedNote;
    }

    public void deleteNote(Long id){
        log.info("Deleting note {}", id );
        Note noteToDelete = findNoteById(id);
        noteToDelete.setDeleted(true);
        noteToDelete.setDeletedAt(LocalDateTime.now());
        noteRepository.save(noteToDelete);
        log.debug("Success - Note {} deleted.", id);
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
                .map(NoteDto::new)
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

    public User getUser(String username){
        return userRepository.findByUsername(username).orElseThrow(()->{
            log.warn("Fail - User {} not found.", username);
            return new UserNotFoundException("User not found");
        });
    }
}
