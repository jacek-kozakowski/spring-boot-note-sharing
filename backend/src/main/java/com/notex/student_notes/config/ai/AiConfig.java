package com.notex.student_notes.config.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiConfig {

    @Bean
    @ConditionalOnExpression("'${spring.ai.openai.api-key:}'.length() > 0")
    public OpenAiChatModel openAiChatModel(
            @Value("${spring.ai.openai.api-key}") String apiKey) {
        log.info("AI: Creating OpenAiChatModel with API key");
        return new OpenAiChatModel(new OpenAiApi(apiKey));
    }

    @Bean
    @ConditionalOnExpression("'${spring.ai.openai.api-key:}'.length() > 0")
    public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
        log.info("AI: Using real OpenAI ChatClient");
        return ChatClient.builder(openAiChatModel).build();
    }

    @Bean
    @ConditionalOnExpression("'${spring.ai.openai.api-key:}'.length() == 0")
    public ChatClient noOpChatClient() {
        log.warn("AI: Using NoOp ChatClient (no API key configured)");
        return ChatClient.builder(new NoOpClient()).build();
    }
}
