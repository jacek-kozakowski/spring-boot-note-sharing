package com.notex.student_notes.note.controller;

import com.notex.student_notes.note.dto.CreateNoteDto;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.dto.UpdateNoteDto;
import com.notex.student_notes.note.service.NoteService;
import com.notex.student_notes.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/notes")
@Validated
@Slf4j
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDto> getNoteById(@PathVariable Long noteId){
        log.info("GET /notes/{}: Fetching note.", noteId);
        NoteDto note = noteService.getNoteById(noteId);
        ResponseEntity<NoteDto> response = ResponseEntity.ok(note);
        log.debug("Success - GET /notes/{}: Fetched note.", noteId);
        return response;
    }
    @GetMapping
    public ResponseEntity<List<NoteDto>> getNotesByPartialName(@RequestParam(required = false) String partialName){
        log.info("GET /notes?name={}: Fetching notes.", partialName);
        List<NoteDto> notes = noteService.getNotesByPartialName(partialName);
        ResponseEntity<List<NoteDto>> response = ResponseEntity.ok(notes);
        log.debug("Success - GET /notes?name={}: Fetched notes.", partialName);
        return response;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<NoteDto> createNote(@ModelAttribute @Validated CreateNoteDto inputNote){
        User currentUser = getCurrentUser();
        log.info("POST /notes: User {} creating a note", currentUser.getUsername());
        NoteDto response = noteService.createNote(inputNote, currentUser);
        log.debug("Success - POST /notes: User {} created a note", currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(value = "/{noteId}", consumes = {"multipart/form-data"})
    public ResponseEntity<NoteDto> updateNote(@PathVariable Long noteId, @ModelAttribute @Validated UpdateNoteDto inputNote){
        log.info("PATCH /notes/{}: User {} updating note.", noteId, getCurrentUser().getUsername());
        User currentUser = getCurrentUser();
        if(!noteService.verifyUserIsOwner(noteId, currentUser)){
            log.warn("Fail - User {} can't update note {}: User is not the owner.", currentUser.getUsername(), noteId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        NoteDto updatedNote = noteService.updateNote(noteId, inputNote);
        log.debug("Success - PATCH /notes/{}: User {} updated note.", noteId, currentUser.getUsername());
        return ResponseEntity.ok(updatedNote);
    }
    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable Long noteId){
        log.info("DELETE /notes/{}: User {} deleting note.", noteId, getCurrentUser().getUsername());
        User currentUser = getCurrentUser();
        if(!noteService.verifyUserIsOwner(noteId, currentUser)){
            log.warn("Fail - User {} can't delete note {}: User is not the owner.", currentUser.getUsername(), noteId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        noteService.deleteNote(noteId);
        log.debug("Success - DELETE /notes/{}: User {} deleted note.", noteId, currentUser.getUsername());
        return ResponseEntity.ok().body("Note successfully deleted");
    }
    @DeleteMapping("/{noteId}/images/{imageId}")
    public ResponseEntity<?> deleteNoteImage(@PathVariable Long noteId, @PathVariable Long imageId){
        log.info("DELETE /notes/{}/images/{}: User {} deleting note image.", noteId, imageId, getCurrentUser().getUsername());
        User currentUser = getCurrentUser();
        if(!noteService.verifyUserIsOwner(noteId, currentUser)){
            log.warn("Fail - User {} can't delete note image {}: User is not the owner.", currentUser.getUsername(), imageId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        noteService.deleteNoteImage(noteId, imageId);
        log.debug("Success - DELETE /notes/{}/images/{}: User {} deleted note image.", noteId, imageId, currentUser.getUsername());
        return ResponseEntity.ok().body("Note image successfully deleted");
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return noteService.getUser(auth.getName());
    }

}
