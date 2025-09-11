package com.notex.student_notes.message.service;


import com.notex.student_notes.group.exceptions.GroupNotFoundException;
import com.notex.student_notes.group.exceptions.UserNotInGroupException;
import com.notex.student_notes.group.model.Group;
import com.notex.student_notes.group.repository.GroupRepository;
import com.notex.student_notes.message.dto.MessageDto;
import com.notex.student_notes.message.dto.SendMessageDto;
import com.notex.student_notes.message.model.Message;
import com.notex.student_notes.message.repository.MessageRepository;
import com.notex.student_notes.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;

    public List<MessageDto> getMessagesByGroupId(Long groupId, User currentUser){
        log.info("Fetching messages for group {}", groupId);
        if (!isUserInGroup(findGroupById(groupId), currentUser)){
            log.warn("Fail - User {} is not in group {}", currentUser.getUsername(), groupId);
            throw new UserNotInGroupException("User is not in group");
        }
        List<Message> groupMessages = messageRepository.findAllByGroupIdOrderByCreatedAtAsc(groupId);
        log.debug("Success - Fetched {} messages for group {}", groupMessages.size(), groupId);
        return groupMessages.stream().map(MessageDto::new).toList();
    }

    @Transactional
    public MessageDto sendMessage(SendMessageDto message, User sender){
        log.info("Sending message {} to group {}", message.getContent(), message.getGroupId());
        Group receivingGroup = findGroupById(message.getGroupId());
        if (!isUserInGroup(receivingGroup, sender)){
            log.warn("Fail - User {} is not in group {}", sender.getUsername(), message.getGroupId());
            throw new UserNotInGroupException("User is not in group");
        }
        Message newMessage = new Message(message.getContent(), sender, receivingGroup);
        Message savedMessage = messageRepository.save(newMessage);
        log.debug("Success - Message sent");
        return new MessageDto(savedMessage);
    }

    private Group findGroupById(Long groupId){
        return groupRepository.findById(groupId).orElseThrow(()->{
            log.warn("Fail - Group {} not found.", groupId);
            return new GroupNotFoundException("Group not found");
        });
    }
    private boolean isUserInGroup(Group group, User user){
        return groupRepository.existsByIdAndMembersId(group.getId(), user.getId());
    }
}
