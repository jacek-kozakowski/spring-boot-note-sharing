package com.notex.student_notes.group.exceptions;

public class GroupAlreadyExistsException extends RuntimeException {
    public GroupAlreadyExistsException(String message) {
        super(message);
    }
}
