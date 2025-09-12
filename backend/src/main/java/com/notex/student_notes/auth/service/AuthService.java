package com.notex.student_notes.auth.service;

import com.notex.student_notes.auth.dto.LoginUserRequestDto;
import com.notex.student_notes.auth.dto.LoginUserResponseDto;
import com.notex.student_notes.auth.dto.RegisterUserDto;
import com.notex.student_notes.auth.dto.VerifyUserDto;
import com.notex.student_notes.auth.exceptions.*;
import com.notex.student_notes.mail.service.EmailService;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private static final int EXPIRATION_TIME_MINUTES = 15;
    private static final int CODE_MIN = 100000;
    private static final int CODE_MAX = 900000;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserDto register(RegisterUserDto input){
        log.info("Registering user with username: {} ", input.getUsername());
        if(userRepository.existsByUsername(input.getUsername())){
            log.info("Failed to register - User with this username already exist");
            throw new UserAlreadyExistsException("User already exists with this username");
        }
        if(userRepository.existsByEmail(input.getEmail())){
            log.info("Failed to register - User with this email already exist");
            throw new UserAlreadyExistsException("User already exists with this email");
        }
        User newUser = new User(input.getUsername(), passwordEncoder.encode(input.getPassword()), input.getEmail(), input.getFirstName(), input.getLastName());
        newUser.setVerificationCode(generateVerificationCode());
        newUser.setVerificationExpiration(LocalDateTime.now().plusMinutes(EXPIRATION_TIME_MINUTES));
        sendVerificationEmail(newUser);
        User savedUser = userRepository.save(newUser);
        log.debug("User with username: {} saved successfully", savedUser.getUsername());
        return new UserDto(savedUser);
    }


    public LoginUserResponseDto authenticate(LoginUserRequestDto input){
        log.info("Authenticating user {}", input.getUsername());
        Optional<User> userOptional = userRepository.findByUsername(input.getUsername());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(!user.isEnabled()){
                log.warn("Fail - Can't authenticate. User {} is not verified", user.getUsername());
                throw new UserNotVerifiedException("User is not verified");
            }
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(input.getUsername(), input.getPassword()));
            LoginUserResponseDto response = createLoginResponse(user);
            log.debug("Success - User {} authenticated ", user.getUsername());
            return response;
        }else{
            log.error("Error - Can't authenticate. User {} doesn't exist.", input.getUsername());
            throw new UserNotFoundException("User not found with this username");
        }
    }

    @Transactional
    public void verifyUser(VerifyUserDto input){
        log.info("Verifying user: {}", input.getUsername());
        Optional<User> userOptional = userRepository.findByUsername(input.getUsername());
        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(user.isEnabled()){
                log.warn("Fail - User {} is already verified", user.getUsername());
                throw new UserAlreadyVerifiedException(String.format("User %s is already verified", user.getUsername()));
            }
            if(!Objects.equals(user.getVerificationCode(), input.getVerificationCode())){
                log.warn("Fail - Invalid verification code for user {}", user.getUsername());
                throw new InvalidVerificationCodeException(String.format("Invalid verification code for user %s", user.getUsername()));
            }
            if(user.getVerificationExpiration().isBefore(LocalDateTime.now())){
                log.warn("Fail - Verification code is expired for user {}", user.getUsername());
                throw new VerificationCodeExpiredException(String.format("Invalid verification code for user %s", user.getUsername()));
            }
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationExpiration(null);
            userRepository.save(user);
            log.debug("Success - User {} verified successfully", user.getUsername());
        }else{
            log.error("Error - Can't verify. User {} doesn't exist", input.getUsername());
            throw new UserNotFoundException("User not found with this username");
        }

    }

    @Transactional
    public void resendVerificationEmail(String username){
        log.info("Resending verification email to user {}", username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(!user.isEnabled()){
                user.setVerificationCode(generateVerificationCode());
                user.setVerificationExpiration(LocalDateTime.now().plusMinutes(EXPIRATION_TIME_MINUTES));
                userRepository.save(user);
                sendVerificationEmail(user);
                log.debug("Success - Successfully resent verification code to user {}", username);
            }else{
                log.error("Error - User {} is already verified", username);
                throw new UserAlreadyVerifiedException(String.format("User %s is already verified", username));
            }
        }else{
            log.warn("Fail - User {} not found", username);
            throw new UserNotFoundException("User not found with this username");
        }
    }

    private void sendVerificationEmail(User user){
        String email = user.getEmail();
        log.info("Trying to send verification code to user {}", email);
        String subject = "Account Verification";
        String verificationCode = user.getVerificationCode();
        if(verificationCode == null || email == null) {
            log.error("Cannot send email - missing required data");
            throw new IllegalArgumentException("Missing verification code or email");
        }
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to NotEx!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
        try{
            emailService.sendVerificationEmail(email, subject, htmlMessage);
            log.debug("Success - Verification code sent to user {}", email);
        }catch (MessagingException e ){
            log.error("Error - Failed to send verification code to user {}", email);
            throw new EmailSendingException("Unable to send verification code", e);
        }
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        return String.valueOf(random.nextInt(CODE_MAX) + CODE_MIN);
    }

    private LoginUserResponseDto createLoginResponse(User user){
        String jwtToken = jwtService.generateToken(user);
        Long jwtExpiration = jwtService.getExpirationTime();
        return new LoginUserResponseDto(jwtToken, jwtExpiration);
    }
}
