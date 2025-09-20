package com.notex.student_notes.auth.controller;


import com.notex.student_notes.auth.dto.*;
import com.notex.student_notes.auth.service.AuthService;
import com.notex.student_notes.config.RateLimitingService;
import com.notex.student_notes.group.dto.ApiResponse;
import com.notex.student_notes.user.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final RateLimitingService rateLimitingService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid RegisterUserDto input, HttpServletRequest request){
        String remoteAddress = request.getRemoteAddr();
        rateLimitingService.checkRateLimit(remoteAddress, "/auth/register",5, 1);
        log.info("POST /auth/register: Registering user {}.", input.getUsername());
        UserDto response = authService.register(input);
        log.debug("Success - POST /auth/register: Registered user {}.", input.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginUserResponseDto> login(@RequestBody @Valid LoginUserRequestDto input){
        log.info("POST /auth/login: Authenticating user {}.", input.getUsername());
        LoginUserResponseDto response = authService.authenticate(input);
        log.debug("Success - POST /auth/login: Authenticated user {}.", input.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verify(@RequestBody @Valid VerifyUserDto input){
        log.info("POST /auth/verify: Verifying user {}.", input.getUsername());
        authService.verifyUser(input);
        log.debug("Success - POST /auth/verify: Verified user {}.", input.getUsername());
        return ResponseEntity.ok(new ApiResponse("User verified successfully."));
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse> resendVerification(@RequestBody @Valid ResendVerificationDto input, HttpServletRequest request){
        String remoteAddress = request.getRemoteAddr();
        rateLimitingService.checkRateLimit(remoteAddress, "/auth/resend",5, 1);
        log.info("POST /auth/resend: Resending verification email to user {}", input.getUsername());
        authService.resendVerificationEmail(input.getUsername());
        log.debug("Success - POST /auth/resend: Resent verification email.");
        return ResponseEntity.ok(new ApiResponse("Verification code was sent successfully. Check your email."));
    }
}
