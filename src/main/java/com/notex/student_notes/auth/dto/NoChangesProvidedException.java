package com.notex.student_notes.auth.dto;

public class NoChangesProvidedException extends RuntimeException {
    public NoChangesProvidedException(String message) {
        super(message);
    }
}
