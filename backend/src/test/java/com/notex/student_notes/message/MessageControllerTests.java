package com.notex.student_notes.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notex.student_notes.auth.security.JwtAuthFilter;
import com.notex.student_notes.group.model.Group;
import com.notex.student_notes.message.controller.MessageController;
import com.notex.student_notes.message.dto.MessageDto;
import com.notex.student_notes.message.dto.SendMessageDto;
import com.notex.student_notes.message.service.MessageService;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = MessageController.class,
        excludeFilters = @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, classes= JwtAuthFilter.class)
)
public class MessageControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;
    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    private Group mockGroup;

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
    @WithMockUser(username = "testuser", roles = "USER")
    void sendMessage_ShouldSendMessage_WhenDataIsValid() throws Exception {
        SendMessageDto input = new SendMessageDto();
        input.setGroupId(1L);
        input.setContent("Test message");

        MessageDto response = new MessageDto();
        response.setId(1L);
        response.setContent("Test message");
        response.setGroupId(1L);
        response.setAuthor("testuser");
        response.setCreatedAt(LocalDateTime.now());

        when(userService.getUserEntityByUsername("testuser")).thenReturn(mockUser);
        when(messageService.sendMessage(input, mockUser)).thenReturn(response);

        mockMvc.perform(post("/groups/1/messages")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").value("Test message"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.author").value("testuser"))
                .andExpect(jsonPath("$.groupId").value(1L));
    }
}
