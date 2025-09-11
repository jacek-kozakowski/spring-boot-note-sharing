package com.notex.student_notes.group.controller;

import com.notex.student_notes.group.dto.*;
import com.notex.student_notes.group.exceptions.ForbiddenOperationException;
import com.notex.student_notes.group.service.GroupService;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@Slf4j
@RequiredArgsConstructor
@Validated
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<GroupDto>> getGroupsByName(@RequestParam String name){
        log.info("GET /groups?name={}: Fetching group.", name);
        List<GroupDto> group = groupService.getGroupsByPartialName(name);
        log.debug("Success - GET /groups?name={}: Fetched group.", name);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable Long groupId){
        log.info("GET /groups/{}: Fetching group.", groupId);
        GroupDto group = groupService.getGroupById(groupId);
        log.debug("Success - GET /groups/{}: Fetched group.", groupId);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<UserDto>> getUsersInGroup(@PathVariable @Positive Long groupId){
        User currentUser = getCurrentUser();
        log.info("GET /groups/{}/members: Fetching members in group.", groupId);
        List<UserDto> members = groupService.getUsersInGroup(groupId, currentUser);
        log.debug("Success - GET /groups/{}/members: Fetched members in group.", groupId);
        return ResponseEntity.ok(members);

    }
    @PostMapping
    public ResponseEntity<GroupDto> createGroup(@RequestBody @Valid CreateGroupDto input){
        User currentUser = getCurrentUser();
        log.info("POST /groups: User {} creating group {}.", currentUser.getUsername(),  input.getName());
        GroupDto createdGroup = groupService.createGroup(input, currentUser);
        log.debug("Success - POST /groups: User {} created group {}.", currentUser.getUsername(),  input.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupDto> updateGroup(@PathVariable @Positive Long groupId, @RequestBody @Valid UpdateGroupDto input){
        User currentUser = getCurrentUser();
        log.info("PATCH /groups/{}: User {} updating group.", groupId, currentUser.getUsername());
        GroupDto updatedGroup = groupService.updateGroup(groupId, input, currentUser);
        log.debug("Success - PATCH /groups/{}: User {} updated group.", groupId, currentUser.getUsername());
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable @Positive Long groupId){
        User currentUser = getCurrentUser();
        log.info("DELETE /groups/{}: User {} deleting group.", groupId, currentUser.getUsername());
        groupService.deleteGroupById(groupId, currentUser);
        log.debug("Success - DELETE /groups/{}: User {} deleted group.", groupId, currentUser.getUsername());
        return ResponseEntity.ok().body(new ApiResponse("Group deleted successfully"));
    }

    @PostMapping("/{groupId}/members/{username}")
    public ResponseEntity<ApiResponse> addUserToGroup(@PathVariable @Positive Long groupId, @PathVariable String username){
        User currentUser = getCurrentUser();
        log.info("POST /groups/{}/members/{}: User {} adding member", groupId, username, currentUser.getUsername());
        groupService.addUserToGroup(groupId, username, currentUser);
        log.debug("Success - POST /groups/{}/members/{}: User {} added member to group.", groupId, username, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("User added to group successfully"));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse> joinGroup(@PathVariable @Positive Long groupId, @RequestBody JoinGroupRequestDto request){
        User currentUser = getCurrentUser();
        log.info("POST /groups/{}/members: User {} joining group.", groupId, currentUser.getUsername());
        groupService.joinGroup(groupId, request, currentUser);
        log.debug("Success - POST /groups/{}/members: User {} joined group.", groupId, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("User joined group successfully"));
    }

    @DeleteMapping("/{groupId}/members/{username}")
    public ResponseEntity<ApiResponse> removeUserFromGroup(@PathVariable @Positive Long groupId, @PathVariable String username){
        User currentUser = getCurrentUser();
        log.info("DELETE /groups/{}/members/{}: User {} removing member from group.", groupId,username, currentUser.getUsername());
        groupService.removeUserFromGroup(groupId, username, currentUser);
        log.debug("Success - DELETE /groups/{}/members/{}: User {} removed member from group.", groupId, username, currentUser.getUsername());
        return ResponseEntity.ok().body(new ApiResponse("Member removed from group successfully"));
    }

    @DeleteMapping("/{groupId}/members/me")
    public ResponseEntity<ApiResponse> leaveGroup(@PathVariable @Positive Long groupId){
        User currentUser = getCurrentUser();
        log.info("DELETE /groups/{}/members/me: User {} leaving group.", groupId, currentUser.getUsername());
        groupService.leaveGroup(groupId, currentUser);
        log.debug("Success - DELETE /groups/{}/members/me: User {} left group.", groupId, currentUser.getUsername());
        return ResponseEntity.ok().body(new ApiResponse("User left group successfully"));
    }

    private User getCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserEntityByUsername(auth.getName());
    }
}
