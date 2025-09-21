package com.notex.student_notes.ai.summary.exceptions;

public class SummaryGenerationFailedException extends RuntimeException{

    public SummaryGenerationFailedException(String message){
        super(message);
    }
}
