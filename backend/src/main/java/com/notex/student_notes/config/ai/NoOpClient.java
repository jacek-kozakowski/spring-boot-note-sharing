package com.notex.student_notes.config.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

public class NoOpClient implements ChatModel {

    @Override
    public ChatResponse call(Prompt prompt) {
        throw new AiNotEnabledException("AI functionality is not enabled. Please configure the API key to use AI features.");
    }

}
