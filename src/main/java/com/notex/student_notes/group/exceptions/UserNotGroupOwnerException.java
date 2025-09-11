package com.notex.student_notes.group.exceptions;

public class UserNotGroupOwnerException extends RuntimeException {
    public UserNotGroupOwnerException(String message) {
        super(message);
    }
}
