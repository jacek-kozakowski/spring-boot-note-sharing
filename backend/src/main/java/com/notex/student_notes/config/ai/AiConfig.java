package com.notex.student_notes.config.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.ai.openai.api-key")
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        log.info("AI: Using real OpenAI ChatClient (api key provided)");
        return ChatClient.builder(openAiChatModel).build();
    }

    @Bean
    @ConditionalOnMissingBean(ChatClient.class)
    public ChatClient noOpChatClient() {
        log.warn("AI: Using NoOp ChatClient (no API key configured)");
        return ChatClient.builder(new NoOpClient()).build();
    }
}
