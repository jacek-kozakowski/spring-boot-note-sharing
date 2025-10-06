package com.notex.student_notes.group;

import com.notex.student_notes.group.dto.CreateGroupDto;
import com.notex.student_notes.group.dto.GroupDto;
import com.notex.student_notes.group.dto.JoinGroupRequestDto;
import com.notex.student_notes.group.dto.UpdateGroupDto;
import com.notex.student_notes.group.exceptions.*;
import com.notex.student_notes.group.model.Group;
import com.notex.student_notes.group.repository.GroupRepository;
import com.notex.student_notes.group.service.GroupService;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTests {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GroupService groupService;

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
        mockGroup.setPrivate(true);
        mockGroup.setPassword("encodedPassword");
        mockGroup.setOwner(mockUser);
        mockGroup.setDeleted(false);
        mockGroup.setDeletedAt(null);
    }

    @Test
    void createGroup_ShouldCreateGroup_WhenDataIsValid(){
        CreateGroupDto input = new CreateGroupDto();
        input.setName("test");
        input.setDescription("test description");
        input.setPrivate(true);
        input.setPassword("password123");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(groupRepository.existsByName(anyString())).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenAnswer(i->i.getArgument(0));

        GroupDto response = groupService.createGroup(input, mockUser);

        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(groupCaptor.capture());
        Group savedGroup = groupCaptor.getValue();

        assertEquals("test", savedGroup.getName());
        assertEquals("test description", savedGroup.getDescription());
        assertTrue(savedGroup.isPrivate());
        assertEquals("encodedPassword", savedGroup.getPassword());
        assertEquals("testuser", savedGroup.getOwner().getUsername());
        assertEquals("testuser", savedGroup.getOwner().getUsername());

        verify(groupRepository, times(1)).existsByName(anyString());
        verify(groupRepository, times(1)).save(any(Group.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void createGroup_ShouldThrowException_WhenNameAlreadyExists(){
        CreateGroupDto input = new CreateGroupDto();
        input.setName("test");
        input.setDescription("test description");
        input.setPrivate(true);
        input.setPassword("password123");

        when(groupRepository.existsByName(anyString())).thenReturn(true);
        GroupAlreadyExistsException ex = assertThrows(GroupAlreadyExistsException.class, ()->{
            groupService.createGroup(input, mockUser);
        });

        assertEquals("Group with this name already exists", ex.getMessage());

        verify(groupRepository, times(1)).existsByName(anyString());
        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void createGroup_ShouldThrowException_WhenPasswordIsNull(){
        CreateGroupDto input = new CreateGroupDto();
        input.setName("test");
        input.setDescription("test description");
        input.setPrivate(true);

        when(groupRepository.existsByName(anyString())).thenReturn(false);
        InvalidGroupCreateRequestException ex = assertThrows(InvalidGroupCreateRequestException.class, ()->{
            groupService.createGroup(input, mockUser);
        });

        assertEquals("Group is private but no password was provided", ex.getMessage());
        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteGroup_ShouldDeleteGroup_WhenGroupFound(){
        mockGroup.setOwner(mockUser);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.save(any(Group.class))).thenAnswer(i->i.getArgument(0));
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);

        groupService.deleteGroupById(1L, mockUser);

        assertTrue(mockGroup.isDeleted());
        assertNotNull(mockGroup.getDeletedAt());

        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void deleteGroup_ShouldThrowException_WhenGroupAlreadyDeleted(){
        mockGroup.setOwner(mockUser);
        mockGroup.setDeleted(true);
        mockGroup.setDeletedAt(LocalDateTime.now());

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);

        GroupDeletedException ex = assertThrows(GroupDeletedException.class, ()->groupService.deleteGroupById(1L, mockUser));

        assertEquals("Group was deleted", ex.getMessage());

        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void updateGroup_ShouldUpdateGroup_WhenGroupFound(){
        mockGroup.setPrivate(false);
        mockGroup.setPassword(null);

        UpdateGroupDto input = new UpdateGroupDto();
        input.setName("new name");
        input.setDescription("new description");
        input.setIsPrivate(true);
        input.setPassword("password123");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        when(groupRepository.save(any(Group.class))).thenAnswer(i->i.getArgument(0));

        GroupDto response = groupService.updateGroup(1L, input, mockUser);

        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupRepository).save(groupCaptor.capture());
        Group updatedGroup = groupCaptor.getValue();

        assertEquals("new name", updatedGroup.getName());
        assertEquals("new name", response.getName());
        assertEquals("new description", updatedGroup.getDescription());
        assertEquals("new description", response.getDescription());
        assertTrue(updatedGroup.isPrivate());
        assertTrue(response.isPrivate());
        assertEquals("encodedPassword", updatedGroup.getPassword());

        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).save(any(Group.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void updateGroup_ShouldThrowException_WhenRequestIsEmpty(){
        UpdateGroupDto input = new UpdateGroupDto();

        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        InvalidGroupUpdateRequestException ex = assertThrows(InvalidGroupUpdateRequestException.class, ()->groupService.updateGroup(1L, input, mockUser));

        assertEquals("Empty request. Can't update group.", ex.getMessage());

        verify(groupRepository, never()).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateGroup_ShouldThrowException_WhenGroupIsAlreadyPrivate(){
        UpdateGroupDto input = new UpdateGroupDto();
        input.setIsPrivate(true);
        input.setPassword("password123");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        InvalidGroupUpdateRequestException ex = assertThrows(InvalidGroupUpdateRequestException.class, ()->groupService.updateGroup(1L, input, mockUser));
        assertEquals("Group is already private", ex.getMessage());

        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void upgradeGroup_ShouldThrowException_WhenChangeToPrivateAndPasswordIsNull(){
        mockGroup.setPrivate(false);
        mockGroup.setPassword(null);

        UpdateGroupDto input = new UpdateGroupDto();
        input.setIsPrivate(true);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        InvalidGroupUpdateRequestException ex = assertThrows(InvalidGroupUpdateRequestException.class, ()->groupService.updateGroup(1L, input, mockUser));
        assertEquals("Can't set group to private without password", ex.getMessage());

        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateGroup_ShouldThrowException_WhenGroupNotFound(){
        UpdateGroupDto input = new UpdateGroupDto();
        input.setName("new name");
        input.setDescription("new description");
        input.setIsPrivate(true);
        input.setPassword("password123");

        when(groupRepository.findById(1L)).thenReturn(Optional.empty());
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);

        GroupNotFoundException ex = assertThrows(GroupNotFoundException.class, ()-> groupService.updateGroup(1L, input, mockUser));

        assertEquals("Group not found", ex.getMessage());

        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateGroup_ShouldThrowException_WhenGroupIsDeleted(){
        mockGroup.setDeleted(true);
        mockGroup.setDeletedAt(LocalDateTime.now());

        UpdateGroupDto input = new UpdateGroupDto();
        input.setName("new name");
        input.setDescription("new description");
        input.setIsPrivate(true);
        input.setPassword("password123");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        GroupDeletedException ex = assertThrows(GroupDeletedException.class, ()-> groupService.updateGroup(1L, input, mockUser));

        assertEquals("Group was deleted", ex.getMessage());
        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void joinGroup_ShouldJoinGroup_WhenGroupFound(){
        JoinGroupRequestDto request = new JoinGroupRequestDto();
        request.setPassword("password123");

        User joiningUser = new User();
        joiningUser.setUsername("joininguser");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        groupService.joinGroup(1L, request, joiningUser);

        assertEquals(2, mockGroup.getMembers().size());
        verify(groupRepository, times(1)).save(any(Group.class));
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());

    }

    @Test
    void joinGroup_ShouldThrowException_WhenGroupNotFound(){
        JoinGroupRequestDto request = new JoinGroupRequestDto();
        request.setPassword("password123");

        User joiningUser = new User();
        joiningUser.setUsername("joininguser");

        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        GroupNotFoundException ex = assertThrows(GroupNotFoundException.class, ()->groupService.joinGroup(1L, request, joiningUser));

        assertEquals("Group not found", ex.getMessage());
        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void joinGroup_ShouldThrowException_WhenUserAlreadyInGroup(){
        JoinGroupRequestDto request = new JoinGroupRequestDto();
        request.setPassword("password123");

        User joiningUser = new User();
        joiningUser.setUsername("joininguser");
        joiningUser.setId(2L);

        mockGroup.addMember(joiningUser);

        when(groupRepository.existsByIdAndMembersId(anyLong(), anyLong())).thenReturn(true);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));

        UserAlreadyInGroupException ex = assertThrows(UserAlreadyInGroupException.class, ()->groupService.joinGroup(1L, request, joiningUser));
        assertEquals("User is already in group", ex.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void joinGroup_ShouldThrowException_WhenGroupIsDeleted(){
        JoinGroupRequestDto request = new JoinGroupRequestDto();
        request.setPassword("password123");

        User joiningUser = new User();
        joiningUser.setUsername("joininguser");

        mockGroup.setDeleted(true);
        mockGroup.setDeletedAt(LocalDateTime.now());

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));

        GroupDeletedException ex = assertThrows(GroupDeletedException.class, ()->groupService.joinGroup(1L, request, joiningUser));

        assertEquals("Group was deleted", ex.getMessage());
        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void joinGroup_ShouldThrowException_WhenGroupIsPrivateAndPasswordIsIncorrect(){
        mockGroup.setPrivate(true);
        mockGroup.setPassword("secret123");
        JoinGroupRequestDto request = new JoinGroupRequestDto();
        request.setPassword("password123");

        User joiningUser = new User();
        joiningUser.setUsername("joininguser");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        AddUserRequestInvalidException ex = assertThrows(AddUserRequestInvalidException.class, ()->groupService.joinGroup(1L, request, joiningUser));

        assertEquals("Wrong password", ex.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    void addUserToGroup_ShouldAddUserToGroup_WhenGroupFound(){
        User userToAdd = new User();
        userToAdd.setUsername("userToAdd");

        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userToAdd));

        groupService.addUserToGroup(1L, "userToAdd", mockUser);
        assertEquals(2, mockGroup.getMembers().size());
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void addUserToGroup_ShouldThrowException_WhenUserNotOwner(){
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(false);

        UserNotGroupOwnerException ex = assertThrows(UserNotGroupOwnerException.class, ()->groupService.addUserToGroup(1L, "userToAdd", mockUser));
        assertEquals("User is not group owner", ex.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void addUserToGroup_ShouldThrowException_WhenUserNotFound(){
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, ()->groupService.addUserToGroup(1L, "userToAdd", mockUser));
        assertEquals("User not found", ex.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void addUserToGroup_ShouldThrowException_WhenUserAlreadyInGroup(){
        User userToAdd = new User();
        userToAdd.setUsername("userToAdd");
        userToAdd.setId(2L);
        mockGroup.addMember(userToAdd);

        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userToAdd));

        UserAlreadyInGroupException ex = assertThrows(UserAlreadyInGroupException.class, ()->groupService.addUserToGroup(1L, "userToAdd", mockUser));
        assertEquals("User is already in group", ex.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
    }


    @Test
    void removeUserFromGroup_ShouldRemoveUserFromGroup_WhenGroupFound(){
        User userToRemove = new User();
        userToRemove.setUsername("usertoremove");
        userToRemove.setId(2L);
        mockGroup.addMember(userToRemove);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndOwnerId(1L, 1L)).thenReturn(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userToRemove));
        when(groupRepository.save(any(Group.class))).thenAnswer(i->i.getArgument(0));
        when(groupRepository.existsByIdAndMembersId(1L, 2L)).thenReturn(true);

        groupService.removeUserFromGroup(1L, "usertoremove", mockUser);
        assertEquals(1, mockGroup.getMembers().size());
        verify(groupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void removeUserFromGroup_ShouldThrowException_WhenUserNotOwner(){
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(false);

        UserNotGroupOwnerException ex = assertThrows(UserNotGroupOwnerException.class, ()->groupService.removeUserFromGroup(1L, "userToAdd", mockUser));
        assertEquals("User is not a group owner", ex.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void removeUserFromGroup_ShouldThrowException_WhenUserNotFound(){
        when(groupRepository.existsByIdAndOwnerId(anyLong(), anyLong())).thenReturn(true);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, ()->groupService.removeUserFromGroup(1L, "userToAdd", mockUser));
        assertEquals("User not found", ex.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void removeUserFromGroup_ShouldThrowException_WhenUserNotMember(){
        User userToRemove = new User();
        userToRemove.setUsername("usertoremove");
        userToRemove.setId(2L);

        when(groupRepository.existsByIdAndOwnerId(1L, 1L)).thenReturn(true);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(userToRemove));
        when(groupRepository.existsByIdAndMembersId(1L, 2L)).thenReturn(false);

        UserNotInGroupException ex = assertThrows(UserNotInGroupException.class, ()->groupService.removeUserFromGroup(1L, "userToAdd", mockUser));
        assertEquals("User is not in group", ex.getMessage());

        verify(groupRepository, never()).save(any(Group.class));
    }

    // TODO leaveGroup tests
    @Test
    void leaveGroup_ShouldLeaveGroup_WhenGroupFound(){
        User leavingUser = new User();
        leavingUser.setId(2L);
        leavingUser.setUsername("leavinguser");
        mockGroup.addMember(leavingUser);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndMembersId(anyLong(), anyLong())).thenReturn(true);
        when(groupRepository.save(any(Group.class))).thenAnswer(i->i.getArgument(0));

        groupService.leaveGroup(1L, leavingUser);

        assertEquals(1, mockGroup.getMembers().size());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void leaveGroup_ShouldDeleteGroup_WhenUserIsOwner(){
        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndOwnerId(1L, 1L)).thenReturn(true);
        when(groupRepository.save(any(Group.class))).thenAnswer(i->i.getArgument(0));

        groupService.leaveGroup(1L, mockUser);

        assertTrue(mockGroup.isDeleted());

        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void leaveGroup_ShouldThrowException_WhenUserNotMember(){
        User leavingUser = new User();
        leavingUser.setId(2L);
        leavingUser.setUsername("leavinguser");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(groupRepository.existsByIdAndMembersId(1L, 2L)).thenReturn(false);

        UserNotInGroupException ex = assertThrows(UserNotInGroupException.class, ()->groupService.leaveGroup(1L, leavingUser));

        assertEquals("User is not in group", ex.getMessage());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void leaveGroup_ShouldThrowException_WhenGroupNotFound(){
        User leavingUser = new User();
        leavingUser.setId(2L);
        leavingUser.setUsername("leavinguser");

        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        GroupNotFoundException ex = assertThrows(GroupNotFoundException.class, ()->groupService.leaveGroup(1L, leavingUser));

        assertEquals("Group not found", ex.getMessage());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void leaveGroup_ShouldThrowException_WhenGroupIsDeleted(){
        User leavingUser = new User();
        leavingUser.setId(2L);
        leavingUser.setUsername("leavinguser");
        mockGroup.addMember(leavingUser);
        mockGroup.setDeleted(true);
        mockGroup.setDeletedAt(LocalDateTime.now());

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));

        GroupDeletedException ex = assertThrows(GroupDeletedException.class, ()->groupService.leaveGroup(1L, leavingUser));

        assertEquals("Group was deleted", ex.getMessage());
        verify(groupRepository, never()).save(any(Group.class));
    }
}
