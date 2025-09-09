package com.notex.student_notes.group.exceptions;

public class GroupNotFoundException extends  RuntimeException {
    public GroupNotFoundException(String message) {
        super(message);
    }
}
