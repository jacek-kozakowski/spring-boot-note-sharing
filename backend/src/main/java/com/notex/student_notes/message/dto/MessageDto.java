package com.notex.student_notes.message.dto;

import com.notex.student_notes.message.model.Message;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MessageDto {
    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private Long groupId;

    public MessageDto(Message message){
        this.id = message.getId();
        this.content = message.getContent();
        this.author = message.getSender().getUsername();
        this.createdAt = message.getCreatedAt();
        this.groupId = message.getGroup().getId();
    }
}
