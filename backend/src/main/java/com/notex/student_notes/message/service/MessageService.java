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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;

    public Page<MessageDto> getMessagesByGroupId(Long groupId, User currentUser, Pageable pageable){
        log.info("Fetching messages for group {}", groupId);
        if (!isUserInGroup(findGroupById(groupId), currentUser)){
            log.warn("Fail - User {} is not in group {}", currentUser.getUsername(), groupId);
            throw new UserNotInGroupException("User is not in group");
        }
        Page<Message> groupMessages = messageRepository.findAllByGroupIdOrderByCreatedAtAsc(groupId, pageable);
        log.debug("Success - Fetched {} messages for group {}", groupMessages.stream().count(), groupId);
        return groupMessages.map(MessageDto::new);
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
