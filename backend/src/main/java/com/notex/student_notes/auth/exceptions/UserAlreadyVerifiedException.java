package com.notex.student_notes.auth.exceptions;

public class UserAlreadyVerifiedException extends RuntimeException {
    public UserAlreadyVerifiedException(String message) {
        super(message);
    }
}
