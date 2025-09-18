package com.notex.student_notes.message.controller;

import com.notex.student_notes.config.RateLimitingService;
import com.notex.student_notes.message.dto.MessageDto;
import com.notex.student_notes.message.dto.SendMessageDto;
import com.notex.student_notes.message.service.MessageService;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/groups/{groupId}/messages")
public class MessageController {
    private final MessageService messageService;
    private final UserService userService;
    private final RateLimitingService rateLimitingService;

    @GetMapping
    public ResponseEntity<Page<MessageDto>> getMessages(@PathVariable @Positive Long groupId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        User currentUser = getCurrentUser();
        log.info("GET /groups/{}/messages: Fetching messages for group {}.", groupId, currentUser.getUsername());
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<MessageDto> messages = messageService.getMessagesByGroupId(groupId, currentUser, pageable);
        log.debug("Success - GET /groups/{}/messages: Fetched messages for group {}.", groupId, currentUser.getUsername());
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@PathVariable @Positive Long groupId, @RequestBody @Valid SendMessageDto messageToSend, HttpServletRequest request){
        String remoteAddress = request.getRemoteAddr();
        rateLimitingService.checkRateLimit(remoteAddress, 60, 1); // 60 messages per minute
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
