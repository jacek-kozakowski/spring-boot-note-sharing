package com.notex.student_notes.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notex.student_notes.ai.summary.service.SummaryService;
import com.notex.student_notes.ai.translations.service.TranslationService;
import com.notex.student_notes.group.repository.GroupRepository;
import com.notex.student_notes.group.service.GroupService;
import com.notex.student_notes.message.service.MessageService;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.note.service.NoteService;
import com.notex.student_notes.user.dto.UpdateUserDto;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.repository.UserRepository;
import com.notex.student_notes.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
    "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
    "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private MessageService messageService;

    @MockitoBean
    private GroupRepository groupRepository;

    @MockitoBean
    private NoteRepository noteRepository;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private SummaryService summaryService;

    @MockitoBean
    private TranslationService translationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser")
    void me_ShouldReturnCurrentUser_WhenUserIsAuthenticated() throws Exception {
        UserDto mockUser = new UserDto();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");
        mockUser.setEnabled(true);

        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);

        mockMvc.perform(get("/users/me")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateUser_ShouldUpdateCurrentUser_WhenInputIsValid() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Name");

        UserDto mockUser = new UserDto();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("Updated");
        mockUser.setLastName("Name");
        mockUser.setEnabled(true);

        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);
        when(userService.updateUser(anyString(), any(UpdateUserDto.class))).thenReturn(mockUser);

        mockMvc.perform(patch("/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Name"));
    }

}