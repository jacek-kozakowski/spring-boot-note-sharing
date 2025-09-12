package com.notex.student_notes.user;


import com.notex.student_notes.auth.dto.NoChangesProvidedException;
import com.notex.student_notes.auth.exceptions.SamePasswordException;
import com.notex.student_notes.user.dto.UpdateUserDto;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.Role;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import com.notex.student_notes.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void updateUser_ShouldUpdateUser_WhenUsernameIsAvailable(){
        UpdateUserDto input = new UpdateUserDto();
        input.setUsername("newUsername");

        User mockUser = new User();
        mockUser.setUsername("oldUsername");
        mockUser.setEmail("test@example.com");

        when(userRepository.findByUsername("oldUsername")).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByUsername("newUsername")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i-> i.getArgument(0));

        UserDto userDto = userService.updateUser("oldUsername", input);

        assertEquals("newUsername", userDto.getUsername());
        assertNotEquals("oldUsername", userDto.getUsername());

        verify(userRepository, times(1)).save(any(User.class));
    }
    @Test
    void updateUser_ShouldThrowException_WhenRequestIsEmpty(){
        UpdateUserDto input = new UpdateUserDto();

        NoChangesProvidedException ex = assertThrows(NoChangesProvidedException.class, ()->
                userService.updateUser("test", input));

        assertEquals("Empty request. Can't update user.", ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).findByUsername(anyString());
    }
    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound(){
        UpdateUserDto input = new UpdateUserDto();
        input.setEmail("new@example.com");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () ->
            userService.updateUser("test", input)
        );

        assertEquals("User not found", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenPasswordIsTheSame(){
        UpdateUserDto input = new UpdateUserDto();
        input.setPassword("password123");

        User mockUser = new User();
        mockUser.setUsername("test");
        mockUser.setPassword("encodedPassword");

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        SamePasswordException ex = assertThrows(SamePasswordException.class, ()->
                userService.updateUser("test", input)
        );

        assertEquals("Password matches already used password", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByUsername_ShouldReturnUserDto_WhenUsernameIsValid(){
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test");
        mockUser.setEmail("test@example.com");
        mockUser.setEnabled(true);
        mockUser.setFirstName("First");
        mockUser.setLastName("Last");
        mockUser.setRole(Role.ROLE_USER);

        when(userRepository.findByUsername("test")).thenReturn(Optional.of(mockUser));

        UserDto response = userService.getUserByUsername("test");

        assertEquals(1L, response.getId());
        assertEquals("test", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertTrue(response.isEnabled());
        assertEquals("First", response.getFirstName());
        assertEquals("Last", response.getLastName());
        assertEquals(Role.ROLE_USER, response.getRole());
    }

    @Test
    void getUserByUsername_ShouldThrowException_WhenUserNotFound(){
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, ()->
                userService.getUserByUsername("test")
        );

        assertEquals("User not found", ex.getMessage());
    }
}
