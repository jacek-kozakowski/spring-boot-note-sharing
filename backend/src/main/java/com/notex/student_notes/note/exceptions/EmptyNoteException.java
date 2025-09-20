package com.notex.student_notes.note.exceptions;

public class EmptyNoteException extends RuntimeException {
    public EmptyNoteException(String message) {
        super(message);
    }
}
