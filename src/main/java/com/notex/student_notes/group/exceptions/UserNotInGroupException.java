package com.notex.student_notes.group.exceptions;

public class UserNotInGroupException extends RuntimeException {
    public UserNotInGroupException(String message) {
        super(message);
    }
}
