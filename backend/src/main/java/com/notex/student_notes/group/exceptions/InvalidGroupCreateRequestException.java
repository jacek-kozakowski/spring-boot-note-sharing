package com.notex.student_notes.group.exceptions;

public class InvalidGroupCreateRequestException extends RuntimeException {
    public InvalidGroupCreateRequestException(String message) {
        super(message);
    }
}
