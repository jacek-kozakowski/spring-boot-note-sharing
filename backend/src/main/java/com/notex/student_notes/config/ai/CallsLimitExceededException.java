package com.notex.student_notes.config.ai;

public class CallsLimitExceededException extends RuntimeException {
    public CallsLimitExceededException(String message) {
        super(message);
    }
}
