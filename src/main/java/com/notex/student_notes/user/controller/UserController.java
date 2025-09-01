package com.notex.student_notes.user.controller;

import com.notex.student_notes.user.dto.AdminViewUserDto;
import com.notex.student_notes.user.dto.UpdateUserDto;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        return ResponseEntity.ok(getCurrentUser());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminViewUserDto>> getAllUsers(){
        String username = getCurrentUser().getUsername();
        log.info("GET /users: Admin {} fetching all users", username);
        ResponseEntity<List<AdminViewUserDto>> response =  ResponseEntity.ok(userService.getAllAdminViewUser());
        log.debug("Success - GET /users: Admin {} fetched all users", username);
        return response;
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminViewUserDto> getUserByUsername(@PathVariable String username){
        String adminUsername = getCurrentUser().getUsername();
        log.info("GET /users/{}: Admin {} fetching user.", username, adminUsername);
        ResponseEntity<AdminViewUserDto> response =  ResponseEntity.ok(userService.getAdminViewUserByUsername(username));
        log.debug("Success - GET /users/{}: Admin {} fetched user.", username, adminUsername);
        return response;
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateUser(@RequestBody @Valid UpdateUserDto input){
        String username = getCurrentUser().getUsername();
        log.info("PATCH /users/me: user {} updating information.", username);
        ResponseEntity<UserDto> response = ResponseEntity.ok(userService.updateUser(username,input));
        log.debug("Success - PATCH /users/me: user {} updated their information.", username);
        return response;
    }

    @PatchMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserByAdmin(@PathVariable String username,@RequestBody @Valid UpdateUserDto input){
        String adminUsername = getCurrentUser().getUsername();
        log.info("PATCH /users/{}: Admin {} updating user.", username, adminUsername);
        ResponseEntity<UserDto> response = ResponseEntity.ok(userService.updateUser(username,input));
        log.debug("Success - PATCH /users/{}: Admin {} updated user's information.", username, adminUsername);
        return response;
    }

    private UserDto getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }
}
