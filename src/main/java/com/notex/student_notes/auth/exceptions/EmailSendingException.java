package com.notex.student_notes.auth.exceptions;

public class EmailSendingException extends RuntimeException{
    public EmailSendingException(String message, Throwable cause){
        super(message, cause);
    }
}
