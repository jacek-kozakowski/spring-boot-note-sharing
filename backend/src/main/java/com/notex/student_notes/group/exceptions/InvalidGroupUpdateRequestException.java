package com.notex.student_notes.group.exceptions;

public class InvalidGroupUpdateRequestException extends RuntimeException {
    public InvalidGroupUpdateRequestException(String message) {
        super(message);
    }
}
