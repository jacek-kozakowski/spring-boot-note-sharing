package com.notex.student_notes.group.exceptions;

public class UserAlreadyInGroupException extends RuntimeException {
    public UserAlreadyInGroupException(String message) {
        super(message);
    }
}
