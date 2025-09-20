package com.notex.student_notes.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notex.student_notes.auth.security.JwtAuthFilter;
import com.notex.student_notes.config.RateLimitingService;
import com.notex.student_notes.config.exceptions.RateLimitExceededException;
import com.notex.student_notes.group.controller.GroupController;
import com.notex.student_notes.group.dto.CreateGroupDto;
import com.notex.student_notes.group.dto.GroupDto;
import com.notex.student_notes.group.dto.JoinGroupRequestDto;
import com.notex.student_notes.group.dto.UpdateGroupDto;
import com.notex.student_notes.group.exceptions.UserNotGroupOwnerException;
import com.notex.student_notes.group.model.Group;
import com.notex.student_notes.group.service.GroupService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = GroupController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
public class GroupControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RateLimitingService rateLimitingService;

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
        mockGroup.setPrivate(true);
        mockGroup.setPassword("encodedPassword");
        mockGroup.setCreatedAt(LocalDateTime.now());
        mockGroup.setDeleted(false);
        mockGroup.setDeletedAt(null);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createGroup_ShouldCreateGroup_WhenDataIsValid() throws Exception {
        CreateGroupDto input = new CreateGroupDto();
        input.setName("testgroup");
        input.setDescription("This is a test group");
        input.setPrivate(true);
        input.setPassword("password123");


        when(groupService.createGroup(input, mockUser)).thenReturn(new GroupDto(mockGroup));
        when(userService.getUserEntityByUsername(anyString())).thenReturn(mockUser);

        mockMvc.perform(post("/groups")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("testgroup"))
                .andExpect(jsonPath("$.description").value("This is a test group"))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"))
                .andExpect(jsonPath("$.isPrivate").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createGroup_ShouldReturnTooManyRequests_WhenRateLimitExceeded() throws Exception {
        CreateGroupDto input = new CreateGroupDto();
        input.setName("testgroup");
        input.setDescription("This is a test group");
        input.setPrivate(true);
        input.setPassword("password123");

        doThrow(new RateLimitExceededException("Rate limit exceeded"))
                .when(rateLimitingService).checkRateLimit(anyString(), anyString(),anyInt(), anyInt());

        mockMvc.perform(post("/groups")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateGroup_ShouldUpdateGroup_WhenDataIsValid() throws Exception {
        UpdateGroupDto input = new UpdateGroupDto();
        input.setName("New name");
        input.setDescription("New description");
        input.setIsPrivate(true);
        input.setPassword("password123");

        GroupDto response = new GroupDto(mockGroup);
        response.setName("New name");
        response.setDescription("New description");
        response.setPrivate(true);

        when(groupService.updateGroup(1L, input, mockUser)).thenReturn(response);
        when(userService.getUserEntityByUsername(anyString())).thenReturn(mockUser);

        mockMvc.perform(patch("/groups/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"))
                .andExpect(jsonPath("$.isPrivate").value(true))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateGroup_ShouldReturnTooManyRequests_WhenRateLimitExceeded() throws Exception {
        UpdateGroupDto input = new UpdateGroupDto();
        input.setName("New name");
        input.setDescription("New description");
        input.setIsPrivate(true);
        input.setPassword("password123");

        doThrow(new RateLimitExceededException("Rate limit exceeded"))
                .when(rateLimitingService).checkRateLimit(anyString(), anyString(),anyInt(), anyInt());

        mockMvc.perform(patch("/groups/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteGroup_ShouldReturnOk_WhenGroupExistsAndUserIsOwner() throws Exception {
        when(userService.getUserEntityByUsername(anyString())).thenReturn(mockUser);

        mockMvc.perform(delete("/groups/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Group deleted successfully"));
    }

    @Test
    @WithMockUser(username = "notowner", roles = "USER")
    void deleteGroup_ShouldReturnForbidden_WhenUserIsNotOwner() throws Exception {
        User notOwner = new User();
        notOwner.setId(999L);
        notOwner.setUsername("notowner");

        when(userService.getUserEntityByUsername("notowner")).thenReturn(notOwner);

        doThrow(new UserNotGroupOwnerException("User is not a group owner"))
                .when(groupService).deleteGroupById(1L, notOwner);

        mockMvc.perform(delete("/groups/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void addUserToGroup_ShouldReturnCreated_WhenUserAddedSuccessfully() throws Exception {
        User mockToAdd = new User();
        mockToAdd.setId(2L);
        mockToAdd.setUsername("testuser2");

        when(userService.getUserEntityByUsername("testuser")).thenReturn(mockUser);

        mockMvc.perform(post("/groups/1/members/testuser2")
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User added to group successfully"));
    }

    @Test
    @WithMockUser
    void addUserToGroup_ShouldThrowTooManyRequests_WhenRateLimitExceeded() throws Exception {
        User mockToAdd = new User();
        mockToAdd.setId(2L);
        mockToAdd.setUsername("testuser2");

        doThrow(new RateLimitExceededException("Rate limit exceeded"))
                .when(rateLimitingService).checkRateLimit(anyString(), anyString(),anyInt(), anyInt());

        mockMvc.perform(post("/groups/1/members/testuser2")
                .with(csrf()))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @WithMockUser(username = "notowner", roles = "USER")
    void addUserToGroup_ShouldReturnForbidden_WhenUserIsNotOwner() throws Exception {
        User notOwner = new User();
        notOwner.setId(999L);
        notOwner.setUsername("notowner");

        when(userService.getUserEntityByUsername("notowner")).thenReturn(notOwner);

        doThrow(new UserNotGroupOwnerException("User is not a group owner"))
                .when(groupService).addUserToGroup(1L, "testuser2", notOwner);

        mockMvc.perform(post("/groups/1/members/testuser2")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser2", roles = "USER")
    void joinGroup_ShouldReturnCreated_WhenDataIsValid() throws Exception {
        User mockToAdd = new User();
        mockToAdd.setId(2L);
        mockToAdd.setUsername("testuser2");

        JoinGroupRequestDto input = new JoinGroupRequestDto();
        input.setPassword("password123");

        when(userService.getUserEntityByUsername("testuser2")).thenReturn(mockToAdd);

        mockMvc.perform(post("/groups/1/members")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User joined group successfully"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void removeUserFromGroup_ShouldReturnOk_WhenUserRemovedSuccessfully() throws Exception {
        User userToRemove = new User();
        userToRemove.setId(2L);
        userToRemove.setUsername("testuser2");
        mockGroup.addMember(userToRemove);

        when(userService.getUserEntityByUsername("testuser")).thenReturn(mockUser);

        mockMvc.perform(delete("/groups/1/members/testuser2")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Member removed from group successfully"));
    }

    @Test
    @WithMockUser(username = "notowner", roles = "USER")
    void removeUserFromGroup_ShouldReturnForbidden_WhenUserIsNotOwner() throws Exception {
        User notOwner = new User();
        notOwner.setId(999L);
        notOwner.setUsername("notowner");

        when(userService.getUserEntityByUsername("notowner")).thenReturn(notOwner);

        doThrow(new UserNotGroupOwnerException("User is not a group owner"))
                .when(groupService).removeUserFromGroup(1L, "testuser2", notOwner);

        mockMvc.perform(delete("/groups/1/members/testuser2")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser2", roles = "USER")
    void leaveGroup_ShouldReturnOk_WhenUserRemovedSuccessfully() throws Exception {
        User userToRemove = new User();
        userToRemove.setId(2L);
        userToRemove.setUsername("testuser2");
        mockGroup.addMember(userToRemove);

        when(userService.getUserEntityByUsername("testuser2")).thenReturn(userToRemove);

        mockMvc.perform(delete("/groups/1/members/me")
                        .with(csrf())
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User left group successfully"));
    }
}
