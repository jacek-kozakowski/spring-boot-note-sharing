package com.notex.student_notes.config.ai;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

public class NoOpClient implements ChatModel {

    @Override
    public ChatResponse call(Prompt prompt) {
        String fallback = "AI is disabled: no API key configured. Please enable AI to use this feature.";
        Generation gen = new Generation(new AssistantMessage(fallback));
        return new ChatResponse(java.util.List.of(gen));
    }

}
