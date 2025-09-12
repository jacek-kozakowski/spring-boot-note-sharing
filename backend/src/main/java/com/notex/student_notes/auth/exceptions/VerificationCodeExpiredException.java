package com.notex.student_notes.auth.exceptions;

public class VerificationCodeExpiredException extends RuntimeException {
    public VerificationCodeExpiredException(String message) {
        super(message);
    }
}
