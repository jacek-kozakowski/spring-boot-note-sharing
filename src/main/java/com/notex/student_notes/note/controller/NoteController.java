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


@RestController
@RequestMapping("/notes")
@Validated
@Slf4j
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> getNoteById(@PathVariable Long id){
        log.info("GET /notes/{}: Fetching note.", id);
        NoteDto note = noteService.getNoteById(id);
        ResponseEntity<NoteDto> response = ResponseEntity.ok(note);
        log.debug("Success - GET /notes/{}: Fetched note.", id);
        return response;
    }

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@RequestBody @Validated CreateNoteDto inputNote){
        User currentUser = getCurrentUser();
        log.info("POST /notes: User {} creating a note", currentUser.getUsername());
        NoteDto response = noteService.createNote(inputNote, currentUser);
        log.debug("Success - POST /notes: User {} created a note", currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable Long id, @RequestBody @Validated UpdateNoteDto inputNote){
        log.info("PATCH /notes/{}: User {} updating note.", id, getCurrentUser().getUsername());
        User currentUser = getCurrentUser();
        if(!noteService.verifyUserIsOwner(id, currentUser)){
            log.warn("Fail - User {} can't update note {}: User is not the owner.", currentUser.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        NoteDto updatedNote = noteService.updateNote(id, inputNote);
        log.debug("Success - PATCH /notes/{}: User {} updated note.", id, currentUser.getUsername());
        return ResponseEntity.ok(updatedNote);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNote(@PathVariable Long id){
        log.info("DELETE /notes/{}: User {} deleting note.", id, getCurrentUser().getUsername());
        User currentUser = getCurrentUser();
        if(!noteService.verifyUserIsOwner(id, currentUser)){
            log.warn("Fail - User {} can't delete note {}: User is not the owner.", currentUser.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        noteService.deleteNote(id);
        log.debug("Success - DELETE /notes/{}: User {} deleted note.", id, currentUser.getUsername());
        return ResponseEntity.ok().body("Note successfully deleted");
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return noteService.getUser(auth.getName());
    }

}
