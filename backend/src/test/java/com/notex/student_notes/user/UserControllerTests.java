package com.notex.student_notes.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notex.student_notes.group.repository.GroupRepository;
import com.notex.student_notes.group.service.GroupService;
import com.notex.student_notes.message.service.MessageService;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.note.service.NoteService;
import com.notex.student_notes.summary.service.SummaryService;
import com.notex.student_notes.upload.repository.UploadTaskRepository;
import com.notex.student_notes.upload.service.AsyncUploadService;
import com.notex.student_notes.upload.service.UploadService;
import com.notex.student_notes.user.dto.AdminViewUserDto;
import com.notex.student_notes.user.dto.UpdateUserDto;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.model.Role;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import com.notex.student_notes.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private AsyncUploadService asyncUploadService;

    @MockitoBean
    private UploadTaskRepository uploadTaskRepository;

    @MockitoBean
    private NoteRepository noteRepository;

    @MockitoBean
    private UploadService uploadService;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private SummaryService summaryService;

    @Autowired
    private ObjectMapper mapper;

    private UserDto mockUserDto;
    private AdminViewUserDto mockAdminViewUserDto;
    private UpdateUserDto updateUserDto;

    @BeforeEach
    void setUp(){
        mockUserDto = new UserDto();
        mockUserDto.setId(1L);
        mockUserDto.setUsername("testuser");
        mockUserDto.setEmail("test@example.com");
        mockUserDto.setFirstName("Test");
        mockUserDto.setLastName("User");
        mockUserDto.setRole(Role.ROLE_USER);
        mockUserDto.setEnabled(true);

        mockAdminViewUserDto = new AdminViewUserDto();
        mockAdminViewUserDto.setId(1L);
        mockAdminViewUserDto.setUsername("testuser");
        mockAdminViewUserDto.setEmail("test@example.com");
        mockAdminViewUserDto.setFirstName("Test");
        mockAdminViewUserDto.setLastName("User");
        mockAdminViewUserDto.setRole(Role.ROLE_USER);
        mockAdminViewUserDto.setEnabled(true);

        updateUserDto = new UpdateUserDto();
        updateUserDto.setUsername("newusername");
        updateUserDto.setEmail("new@example.com");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void me_ShouldReturnCurrentUser_WhenUserIsAuthenticated() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(mockUserDto);

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }


    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateUser_ShouldUpdateCurrentUser_WhenInputIsValid() throws Exception {
        UserDto updatedUser = new UserDto();
        updatedUser.setId(1L);
        updatedUser.setUsername("newusername");
        updatedUser.setEmail("new@example.com");
        updatedUser.setFirstName("Test");
        updatedUser.setLastName("User");
        updatedUser.setRole(Role.ROLE_USER);
        updatedUser.setEnabled(true);

        when(userService.getUserByUsername("testuser")).thenReturn(mockUserDto);
        when(userService.updateUser(anyString(), any(UpdateUserDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    @WithMockUser(username = "testadmin", roles = "ADMIN")
    void getUserByUsername_ShouldGetUser_WhenAdminAndUserExists() throws Exception {
        User mockAdmin = new User();
        mockAdmin.setUsername("testadmin");
        mockAdmin.setRole(Role.ROLE_ADMIN);

        AdminViewUserDto targetUser = new AdminViewUserDto();
        targetUser.setId(2L);
        targetUser.setUsername("targetuser");
        targetUser.setEmail("target@example.com");
        targetUser.setRole(Role.ROLE_USER);
        targetUser.setFirstName("Target");
        targetUser.setLastName("User");
        targetUser.setEnabled(true);

        when(userService.getUserEntityByUsername(anyString())).thenReturn(mockAdmin);
        when(userService.getAdminViewUserByUsername("targetuser")).thenReturn(targetUser);

        mockMvc.perform(get("/users/targetuser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.username").value("targetuser"))
                .andExpect(jsonPath("$.email").value("target@example.com"))
                .andExpect(jsonPath("$.role").value(Role.ROLE_USER.name()))
                .andExpect(jsonPath("$.firstName").value("Target"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.enabled").value(true));

    }
}
