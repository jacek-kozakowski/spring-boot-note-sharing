package com.notex.student_notes.note.exceptions;

public class UserNotNoteOwner extends RuntimeException {
    public UserNotNoteOwner(String message) {
        super(message);
    }
}
