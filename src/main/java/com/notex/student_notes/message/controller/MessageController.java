package com.notex.student_notes.message.controller;

import com.notex.student_notes.message.dto.MessageDto;
import com.notex.student_notes.message.dto.SendMessageDto;
import com.notex.student_notes.message.service.MessageService;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/groups/{groupId}/messages")
public class MessageController {
    private final MessageService messageService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable @Positive Long groupId){
        User currentUser = getCurrentUser();
        log.info("GET /groups/{}/messages: Fetching messages for group {}.", groupId, currentUser.getUsername());
        List<MessageDto> messages = messageService.getMessagesByGroupId(groupId, currentUser);
        log.debug("Success - GET /groups/{}/messages: Fetched messages for group {}.", groupId, currentUser.getUsername());
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@PathVariable @Positive Long groupId, @RequestBody @Valid SendMessageDto messageToSend){
        User currentUser = getCurrentUser();
        log.info("POST /groups/{}/messages: User {} sending message to group {}.", groupId, currentUser.getUsername(), messageToSend.getContent());
        messageToSend.setGroupId(groupId);
        MessageDto message = messageService.sendMessage(messageToSend, currentUser);
        log.debug("Success - POST /groups/{}/messages: User {} sent message to group {}.", groupId, currentUser.getUsername(), messageToSend.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    private User getCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserEntityByUsername(auth.getName());
    }
}
