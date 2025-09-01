package com.notex.student_notes.auth;

import com.notex.student_notes.auth.dto.LoginUserRequestDto;
import com.notex.student_notes.auth.dto.LoginUserResponseDto;
import com.notex.student_notes.auth.dto.RegisterUserDto;
import com.notex.student_notes.auth.dto.VerifyUserDto;
import com.notex.student_notes.auth.exceptions.UserAlreadyExistsException;
import com.notex.student_notes.auth.exceptions.UserAlreadyVerifiedException;
import com.notex.student_notes.auth.exceptions.UserNotVerifiedException;
import com.notex.student_notes.auth.service.AuthService;
import com.notex.student_notes.auth.service.JwtService;
import com.notex.student_notes.mail.service.EmailService;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_ShouldRegisterUser_WhenDataIsValid() throws MessagingException {
        RegisterUserDto input = new RegisterUserDto();
        input.setUsername("test");
        input.setPassword("password123");
        input.setEmail("test@example.com");
        input.setFirstName("First");
        input.setLastName("Last");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        UserDto response = authService.register(input);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("test", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("First", savedUser.getFirstName());
        assertEquals("Last", savedUser.getLastName());

        assertEquals(response.getId(), savedUser.getId());
        assertEquals(response.getUsername(), savedUser.getUsername());
        assertEquals(response.getEmail(), savedUser.getEmail());
        assertEquals(response.getFirstName(), savedUser.getFirstName());
        assertEquals(response.getLastName(), savedUser.getLastName());

        verify(userRepository).existsByEmail("test@example.com");
        verify(emailService).sendVerificationEmail(
                eq(savedUser.getEmail()),
                anyString(),
                anyString()
        );
    }

    @Test
    void registerUser_ShouldThrowException_WhenUserAlreadyExists() throws MessagingException {
        RegisterUserDto input = new RegisterUserDto();
        input.setUsername("test");
        input.setPassword("password123");
        input.setEmail("test@example.com");
        input.setFirstName("First");
        input.setLastName("Last");

        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(input);
        });

        assertEquals("User already exists with this username", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void authenticate_ShouldAuthenticateUser_WhenDataIsValidAndUserIsVerified(){
        LoginUserRequestDto input = new LoginUserRequestDto();
        input.setUsername("test");
        input.setPassword("password123");

        User mockUser = new User();
        mockUser.setUsername("test");
        mockUser.setPassword("password123");
        mockUser.setEnabled(true);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(any(User.class))).thenReturn("mockToken");
        when(jwtService.getExpirationTime()).thenReturn(3600L);

        LoginUserResponseDto response = authService.authenticate(input);
        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        assertEquals(3600L, response.getTokenExpirationTime());

        verify(jwtService).generateToken(any(User.class));
        verify(jwtService).getExpirationTime();
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound(){
        LoginUserRequestDto input = new LoginUserRequestDto();
        input.setUsername("test");
        input.setPassword("password123");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        UserNotFoundException ex = assertThrows(UserNotFoundException.class , ()->{
            authService.authenticate(input);
        });

        assertEquals("User not found with this username", ex.getMessage());
        verify(jwtService, never()).generateToken(any(User.class));
        verify(jwtService, never()).getExpirationTime();
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserIsNotVerified(){
        LoginUserRequestDto input = new LoginUserRequestDto();
        input.setUsername("test");
        input.setPassword("password123");

        User mockUser = new User();
        mockUser.setUsername("test");
        mockUser.setPassword("password123");
        mockUser.setEnabled(false);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        UserNotVerifiedException ex = assertThrows(UserNotVerifiedException.class, ()->{
            authService.authenticate(input);
        });

        assertEquals("User is not verified", ex.getMessage());
        verify(jwtService, never()).generateToken(any(User.class));
        verify(jwtService, never()).getExpirationTime();
    }

    @Test
    void verify_ShouldEnableUser_WhenDataIsValid(){
        VerifyUserDto input = new VerifyUserDto();
        input.setUsername("test");
        input.setVerificationCode("123456");

        User mockUser = new User();
        mockUser.setUsername("test");
        mockUser.setEnabled(false);
        mockUser.setVerificationCode("123456");
        mockUser.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        authService.verifyUser(input);

        assertTrue(mockUser.isEnabled());
        assertNull(mockUser.getVerificationCode());
        assertNull(mockUser.getVerificationExpiration());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void verify_ShouldThrowException_WhenUserNotFound(){
        VerifyUserDto input = new VerifyUserDto();
        input.setUsername("test");
        input.setVerificationCode("123456");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, ()->{
           authService.verifyUser(input);
        });

        assertEquals("User not found with this username", ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verify_ShouldThrowException_WhenUserAlreadyVerified(){
        VerifyUserDto input = new VerifyUserDto();
        input.setUsername("test");
        input.setVerificationCode("123456");

        User mockUser = new User();
        mockUser.setUsername("test");
        mockUser.setEnabled(true);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        UserAlreadyVerifiedException ex = assertThrows(UserAlreadyVerifiedException.class, ()->{
           authService.verifyUser(input);
        });

        assertEquals(String.format("User %s is already verified", mockUser.getUsername()), ex.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }


}