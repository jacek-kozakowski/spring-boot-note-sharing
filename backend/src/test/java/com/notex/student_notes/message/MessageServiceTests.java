package com.notex.student_notes.message;

import com.notex.student_notes.group.exceptions.GroupDeletedException;
import com.notex.student_notes.group.exceptions.GroupNotFoundException;
import com.notex.student_notes.group.exceptions.UserNotInGroupException;
import com.notex.student_notes.group.model.Group;
import com.notex.student_notes.group.repository.GroupRepository;
import com.notex.student_notes.message.dto.MessageDto;
import com.notex.student_notes.message.dto.SendMessageDto;
import com.notex.student_notes.message.model.Message;
import com.notex.student_notes.message.repository.MessageRepository;
import com.notex.student_notes.message.service.MessageService;
import com.notex.student_notes.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTests {
    @Mock
    private MessageRepository messageRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private MessageService messageService;

    private Group mockGroup;
    private User mockUser;
    @BeforeEach
    void setUp(){
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockGroup = new Group();
        mockGroup.setId(1L);
        mockGroup.setName("testgroup");
        mockGroup.setDescription("This is a test group");
        mockGroup.setOwner(mockUser);
    }

    @Test
    void sendMessage_ShouldReturnMessageDto_WhenMessageSentSuccessfully(){
        SendMessageDto input = new SendMessageDto();
        input.setGroupId(1L);
        input.setContent("Test message");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndMembersId(anyLong(), anyLong())).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenAnswer(i->i.getArgument(0));
        MessageDto response = messageService.sendMessage(input, mockUser);

        assertEquals("Test message", response.getContent());
        assertEquals(1L, response.getGroupId());
        assertEquals("testuser", response.getAuthor());

        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void sendMessage_ShouldThrowException_WhenGroupNotFound(){
        SendMessageDto input = new SendMessageDto();
        input.setGroupId(1L);
        input.setContent("Test message");

        when(groupRepository.findById(1L)).thenReturn(Optional.empty());
        GroupNotFoundException ex = assertThrows(GroupNotFoundException.class, ()-> messageService.sendMessage(input, mockUser));

        assertEquals("Group not found", ex.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void sendMessage_ShouldThrowException_WhenUserNotInGroup(){
        SendMessageDto input = new SendMessageDto();
        input.setGroupId(1L);
        input.setContent("Test message");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndMembersId(anyLong(), anyLong())).thenReturn(false);
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(false);

        UserNotInGroupException ex = assertThrows(UserNotInGroupException.class, ()-> messageService.sendMessage(input, mockUser));
        assertEquals("User is not in group", ex.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void sendMessage_ShouldThrowException_WhenGroupIsDeleted(){
        mockGroup.setDeleted(true);
        mockGroup.setDeletedAt(LocalDateTime.now());

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        SendMessageDto input = new SendMessageDto();
        input.setGroupId(1L);

        GroupDeletedException ex = assertThrows(GroupDeletedException.class, ()-> messageService.sendMessage(input, mockUser));

        assertEquals("Group was deleted", ex.getMessage());

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void getMessagesByGroupId_ShouldReturnPage_WhenMessagesFound() {
        Long groupId = 1L;
        Pageable pageable = mock(Pageable.class);

        Message message = new Message("Hello", mockUser, mockGroup);
        Page<Message> messagePage = Page.empty();
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndMembersId(groupId, mockUser.getId())).thenReturn(true);
        when(messageRepository.findAllByGroupIdOrderByCreatedAtAsc(groupId, pageable)).thenReturn(messagePage);

        Page<MessageDto> result = messageService.getMessagesByGroupId(groupId, mockUser, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(messageRepository, times(1)).findAllByGroupIdOrderByCreatedAtAsc(groupId, pageable);
    }

    @Test
    void getMessagesByGroupId_ShouldThrowException_WhenUserNotInGroup(){

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndMembersId(anyLong(), anyLong())).thenReturn(false);
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(false);

        UserNotInGroupException ex = assertThrows(UserNotInGroupException.class, ()-> messageService.getMessagesByGroupId(1L, mockUser, mock(Pageable.class)));
        assertEquals("User is not in group", ex.getMessage());
        verify(messageRepository, never()).findAllByGroupIdOrderByCreatedAtAsc(anyLong(), any(Pageable.class));
    }
}
