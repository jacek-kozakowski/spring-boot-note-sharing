package com.notex.student_notes.group.service;

import com.notex.student_notes.group.dto.AddUserToGroupRequest;
import com.notex.student_notes.group.dto.CreateGroupDto;
import com.notex.student_notes.group.dto.GroupDto;
import com.notex.student_notes.group.dto.UpdateGroupDto;
import com.notex.student_notes.group.exceptions.*;
import com.notex.student_notes.group.model.Group;
import com.notex.student_notes.group.repository.GroupRepository;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GroupDto getGroupById(Long groupId){
        log.info("Fetching group {}", groupId);
        Group group = findGroupById(groupId);
        log.debug("Success - Fetched group {}", groupId);
        return new GroupDto(group);
    }

    public List<GroupDto> getAllGroupsByUser(User user){
        log.info("Fetching all groups for user {}", user.getUsername());
        List<Group> userGroups = groupRepository.findAllByOwner(user);
        log.debug("Success - Fetched {} groups for user {}", userGroups.size(), user.getUsername());
        return userGroups.stream().map(GroupDto::new).toList();
    }

    public List<User> getUsersInGroup(Long groupId){
        Group group = findGroupById(groupId);
        return group.getMembers().stream().toList();
    }

    @Transactional
    public GroupDto createGroup(CreateGroupDto input, User owner){
        log.info("Creating group {}", input.getName());
        if(groupRepository.existsByName(input.getName())){
            log.warn("Fail - Group with this name already exists");
            throw new GroupAlreadyExistsException("Group with this name already exists");
        }
        Group newGroup = new Group();
        newGroup.setName(input.getName());
        newGroup.setDescription(input.getDescription());
        newGroup.setOwner(owner);
        newGroup.setPrivateGroup(input.isPrivate());
        newGroup.addMember(owner);
        if (input.isPrivate()){
            if (input.getPassword() == null || input.getPassword().isBlank()){
                log.warn("Fail - Password is null or blank");
                throw new InvalidGroupCreateRequestException("Group is private but no password was provided");
            }
            newGroup.setPassword(passwordEncoder.encode(input.getPassword()));
        }
        Group savedGroup = groupRepository.save(newGroup);
        GroupDto savedGroupDto = new GroupDto(savedGroup);
        log.debug("Success - Group {} created", savedGroupDto.getName());
        return savedGroupDto;
    }


    @Transactional
    public void deleteGroupById(Long id){
        log.info("Deleting group {}", id);
        Group groupToDelete = findGroupById(id);
        if (groupToDelete.isDeleted()){
            log.warn("Fail - Group {} is already deleted", id);
            throw new GroupDeletedException("Group was deleted");
        }
        groupToDelete.setDeleted(true);
        groupToDelete.setDeletedAt(LocalDateTime.now());
        groupRepository.save(groupToDelete);
        log.debug("Success - Group {} deleted", id);
    }

    @Transactional
    public GroupDto updateGroup(Long id, UpdateGroupDto input){
        log.info("Updating group {}", id);
        if (!input.hasAny()){
            log.warn("Fail - No data to update");
            throw new InvalidGroupUpdateRequestException("Empty request. Can't update group.");
        }
        Group groupToUpdate = findGroupById(id);
        if (groupToUpdate.isDeleted()){
            log.warn("Fail - Group {} is already deleted", id);
            throw new GroupDeletedException("Group was deleted");
        }
        if (input.hasName()){
            groupToUpdate.setName(input.getName());
        }
        if (input.hasDescription()){
            groupToUpdate.setDescription(input.getDescription());
        }
        if (input.hasIsPrivate()){
            if (input.getIsPrivate()){
                if (groupToUpdate.isPrivateGroup()) {
                    log.warn("Fail - Group is already private");
                    throw new InvalidGroupUpdateRequestException("Group is already private");
                }
                if (!input.hasPassword()){
                    log.warn("Fail - Can't set group to private without password");
                    throw new InvalidGroupUpdateRequestException("Can't set group to private without password");
                }
                groupToUpdate.setPrivateGroup(true);
                groupToUpdate.setPassword(passwordEncoder.encode(input.getPassword()));
            }else{
                if (!groupToUpdate.isPrivateGroup()){
                    log.warn("Fail - Group is already public");
                    throw new InvalidGroupUpdateRequestException("Group is already public");
                }else{
                    if (!input.hasPassword()) {
                        groupToUpdate.setPrivateGroup(false);
                        groupToUpdate.setPassword(null);
                    }
                }
            }
        }
        if (input.hasPassword() && !input.hasIsPrivate()){
            if (!groupToUpdate.isPrivateGroup()){
                log.warn("Fail - Can't set password for public group");
                throw new InvalidGroupUpdateRequestException("Can't set password for public group");
            }else{
                groupToUpdate.setPassword(passwordEncoder.encode(input.getPassword()));
            }
        }
        Group updatedGroup = groupRepository.save(groupToUpdate);
        GroupDto updatedGroupDto = new GroupDto(updatedGroup);
        log.debug("Success - Group {} updated", updatedGroupDto.getName());
        return updatedGroupDto;
    }

    @Transactional
    public void joinGroup(AddUserToGroupRequest request, User user){
        if (request.getGroupId() == null){
            log.warn("Fail - Group id is null");
            throw new AddUserRequestInvalidException("Group id is null");
        }
        log.info("User {} joining group {}", user.getUsername(), request.getGroupId());
        Group group = findGroupById(request.getGroupId());
        if (group.isDeleted()){
            log.warn("Fail - Group {} is deleted", request.getGroupId());
            throw new GroupDeletedException("Group was deleted");
        }
        if (group.isPrivateGroup()){
            if (group.getPassword() == null || !passwordEncoder.matches(request.getPassword(), group.getPassword())){
                log.warn("Fail - Wrong password");
                throw new AddUserRequestInvalidException("Wrong password");
            }else{
                group.addMember(user);
            }
        }else{
            group.addMember(user);
        }
        groupRepository.save(group);
        log.debug("Success - User {} joined group {}", user.getUsername(), request.getGroupId());
    }

    @Transactional
    public void addUserToGroup(Long groupId, String username){
        Group group = findGroupById(groupId);
        User userToAdd = userRepository.findByUsername(username).orElseThrow(()->{
            log.warn("User with username: {} not found", username);
            return new UserNotFoundException("User not found");
        });
        if (isUserInGroup(groupId, username)){
            log.warn("User {} is already in group {}", username, groupId);
            throw new UserAlreadyInGroupException("User is already in group");
        }
        group.addMember(userToAdd);
        groupRepository.save(group);
        log.debug("Success - User {} added to group {}", username, groupId);
    }

    @Transactional
    public void removeUserFromGroup(Long groupId, String username){
        log.info("Removing user {} from group {}", username, groupId);
        Group group = findGroupById(groupId);
        User userToRemove = userRepository.findByUsername(username).orElseThrow(()->{
            log.warn("User with username: {} not found", username);
            return new UserNotFoundException("User not found");
        });
        group.removeMember(userToRemove);
        groupRepository.save(group);
        log.debug("Success - User {} removed from group {}", username, groupId);
    }

    @Transactional
    public void leaveGroup(Long groupId, User user){
        log.info("{} leaving group {}",user.getUsername(),  groupId);
        Group group = findGroupById(groupId);
        if (isUserGroupOwner(groupId, user)){
            log.warn("{} is the owner of group {}. Deleting group.", user.getUsername(), groupId);
            deleteGroupById(groupId);
            return;
        }
        group.removeMember(user);
        groupRepository.save(group);
        log.debug("Success - {} left group {}",user.getUsername(),  groupId);
    }


    public boolean isUserInGroup(Long groupId, String username){
        Group group = findGroupById(groupId);
        return group.getMembers().stream().anyMatch(user -> user.getUsername().equals(username));
    }

    public boolean isUserInGroup(Long groupId, User user){
        Group group = findGroupById(groupId);
        return group.getMembers().stream().anyMatch(user1 -> user1.getId().equals(user.getId()));
    }

    public boolean isUserGroupOwner(Long groupId, User user){
        Group group = findGroupById(groupId);
        return group.getOwner().getId().equals(user.getId());
    }

    private Group findGroupById(Long id){
        return groupRepository.findById(id).orElseThrow(()->{
            log.warn("Group with id: {} not found", id);
            return new GroupNotFoundException("Group not found");
        });
    }
}
