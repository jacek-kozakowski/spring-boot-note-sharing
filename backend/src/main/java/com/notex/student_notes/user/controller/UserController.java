package com.notex.student_notes.user.controller;

import com.notex.student_notes.group.dto.GroupDto;
import com.notex.student_notes.group.service.GroupService;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.service.Filter;
import com.notex.student_notes.note.service.NoteService;
import com.notex.student_notes.user.dto.AdminViewUserDto;
import com.notex.student_notes.user.dto.UpdateUserDto;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.model.Role;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final NoteService noteService;
    private final GroupService groupService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        return ResponseEntity.ok(getCurrentUserDto());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminViewUserDto>> getAllUsers(){
        String username = getCurrentUserDto().getUsername();
        log.info("GET /users: Admin {} fetching all users", username);
        ResponseEntity<List<AdminViewUserDto>> response =  ResponseEntity.ok(userService.getAllAdminViewUser());
        log.debug("Success - GET /users: Admin {} fetched all users", username);
        return response;
    }
    @GetMapping("/me/notes")
    public ResponseEntity<List<NoteDto>> getAllNotes(){
        User currentUser = getCurrentUser();
        log.info("GET /me/notes: User {} fetching all notes", currentUser.getUsername());
        ResponseEntity<List<NoteDto>> response = ResponseEntity.ok(noteService.getUsersNotes(currentUser));
        log.debug("Success - GET /me/notes: User {} fetched all notes", currentUser.getUsername());
        return response;
    }

    @GetMapping("/me/groups")
    public ResponseEntity<List<GroupDto>> getAllGroups(){
        User currentUser = getCurrentUser();
        log.info("GET /users/me/groups: User {} fetching all groups", currentUser.getUsername());
        List<GroupDto> groups = groupService.getUserGroups(currentUser);
        log.debug("Success - GET /users/me/groups: User {} fetched all groups", currentUser.getUsername());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username){
        User currentUser = getCurrentUser();
        if (currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            String adminUsername = currentUser.getUsername();
            log.info("GET /users/{}: Admin {} fetching user.", username, adminUsername);
            AdminViewUserDto user = userService.getAdminViewUserByUsername(username);
            log.debug("Success - GET /users/{}: Admin {} fetched user.", username, adminUsername);
            return ResponseEntity.ok(user);
        } else {
            log.info("GET /users/{}: User {} fetching user.", username, currentUser.getUsername());
            UserDto user = userService.getUserByUsername(username);
            log.debug("Success - GET /users/{}: User {} fetched user.", username, currentUser.getUsername());
            return ResponseEntity.ok(user);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{username}/groups")
    public ResponseEntity<List<GroupDto>> getUserGroups(@PathVariable String username){
        log.info("GET /users/{}/groups: Fetching user's groups.", username);
        List<GroupDto> userGroups = groupService.getAllGroupsByUser(userService.getUserEntityByUsername(username));
        ResponseEntity<List<GroupDto>> response = ResponseEntity.ok(userGroups);
        log.debug("Success - GET /users/{}/groups: Fetched user's groups.", username);
        return response;
    }
    @GetMapping("/{username}/notes")
    public ResponseEntity<List<NoteDto>> getUserNotes(@PathVariable String username){
        log.info("GET /users/{}/notes: Fetching user's notes.", username);
        ResponseEntity<List<NoteDto>> response = ResponseEntity.ok(noteService.getUsersNotes(username));
        log.debug("Success - GET /users/{}/notes: Fetched user's notes.", username);
        return response;
    }
    @GetMapping("/{username}/notes/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NoteDto>> getUserNotesAdmin(@PathVariable String username, @RequestParam(required = false, defaultValue = "all") Filter filter){
        String adminUsername = getCurrentUser().getUsername();
        log.info("GET /users/{}/notes/admin: Admin {} fetching user's notes.", username, adminUsername);
        ResponseEntity<List<NoteDto>> response = ResponseEntity.ok(noteService.getUsersNotesAdmin(username, filter));
        log.debug("Success - GET /users/{}/notes/admin: Admin {} fetched user's notes.", username, adminUsername);
        return response;
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateUser(@RequestBody @Valid UpdateUserDto input){
        String username = getCurrentUserDto().getUsername();
        log.info("PATCH /users/me: user {} updating information.", username);
        ResponseEntity<UserDto> response = ResponseEntity.ok(userService.updateUser(username,input));
        log.debug("Success - PATCH /users/me: user {} updated their information.", username);
        return response;
    }

    @PatchMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserByAdmin(@PathVariable String username,@RequestBody @Valid UpdateUserDto input){
        String adminUsername = getCurrentUserDto().getUsername();
        log.info("PATCH /users/{}: Admin {} updating user.", username, adminUsername);
        ResponseEntity<UserDto> response = ResponseEntity.ok(userService.updateUser(username,input));
        log.debug("Success - PATCH /users/{}: Admin {} updated user's information.", username, adminUsername);
        return response;
    }

    private UserDto getCurrentUserDto(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserByUsername(username);
    }

    private User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.getUserEntityByUsername(username);
    }

}
