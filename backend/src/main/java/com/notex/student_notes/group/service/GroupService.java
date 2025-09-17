package com.notex.student_notes.group.service;

import com.notex.student_notes.group.dto.*;
import com.notex.student_notes.group.exceptions.*;
import com.notex.student_notes.group.model.Group;
import com.notex.student_notes.group.repository.GroupRepository;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public List<GroupDto> getGroupsByPartialName(String partialName){
        log.info("Fetching groups with partial name {}", partialName);
        List<Group> groups = groupRepository.findByNameContainingIgnoreCase(partialName);
        log.debug("Success - Fetched {} groups with partial name {}", groups.size(), partialName);
        return groups.stream().filter(g -> !g.isDeleted()).map(GroupDto::new).toList();
    }

    public List<GroupDto> getGroupsByPartialName(String partialName, User currentUser){
        log.info("Fetching groups with partial name {} for user {}", partialName, currentUser.getUsername());
        List<Group> groups = groupRepository.findByNameContainingIgnoreCase(partialName);
        log.debug("Success - Fetched {} groups with partial name {}", groups.size(), partialName);
        return groups.stream()
                .filter(g -> !g.isDeleted())
                .map(g -> new GroupDto(g, isUserInGroup(g.getId(), currentUser)))
                .toList();
    }

    public List<GroupDto> getAllGroupsByUser(User user){
        log.info("Fetching all groups for user {}", user.getUsername());
        List<Group> userGroups = groupRepository.findAllByOwner(user);
        log.debug("Success - Fetched {} groups for user {}", userGroups.size(), user.getUsername());
        return userGroups.stream().filter(g -> !g.isDeleted()).map(GroupDto::new).toList();
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getUserGroups(User user){
        log.info("Fetching user groups for {}", user.getUsername());

        List<Group> ownedGroups = groupRepository.findAllByOwner(user);
        List<Group> memberGroups = groupRepository.findAllByMembersId(user.getId());
        
        log.debug("Found {} owned groups and {} member groups for user {}", 
                 ownedGroups.size(), memberGroups.size(), user.getUsername());
        
        Set<Group> allGroups = new HashSet<>(ownedGroups);
        allGroups.addAll(memberGroups);
        
        log.debug("Success - Fetched {} groups for user {}", allGroups.size(), user.getUsername());

        return allGroups.stream()
                .filter(g -> !g.isDeleted())
                .map(g -> new GroupDto(g, true))
                .toList();
    }


    public List<GroupDto> getGroupsByOwner(String ownerUsername, User currentUser){
        log.info("Fetching groups by owner {} for user {}", ownerUsername, currentUser.getUsername());
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        List<Group> ownerGroups = groupRepository.findAllByOwner(owner);
        log.debug("Success - Fetched {} groups by owner {}", ownerGroups.size(), ownerUsername);
        return ownerGroups.stream()
                .filter(g -> !g.isDeleted())
                .map(g -> new GroupDto(g, isUserInGroup(g.getId(), currentUser)))
                .toList();
    }

    public List<UserDto> getUsersInGroup(Long groupId, User currentUser){
        Group group = findGroupById(groupId);
        if (!isUserGroupOwner(groupId, currentUser)){
            log.warn("Fail - User {} is not a group owner", currentUser.getUsername());
            throw new UserNotGroupOwnerException("User is not a group owner");
        }
        if (group.isDeleted()){
            log.warn("Fail - Group {} is deleted", groupId);
            throw new GroupDeletedException("Group was deleted");
        }
        return group.getMembers().stream().map(UserDto::new).toList();
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
    public void deleteGroupById(Long id, User currentUser){
        log.info("Deleting group {}", id);
        if (!isUserGroupOwner(id, currentUser)){
            throw new UserNotGroupOwnerException("User is not a group owner");
        }
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
    public GroupDto updateGroup(Long id, UpdateGroupDto input, User currentUser){
        if (!isUserGroupOwner(id, currentUser)){
            throw new UserNotGroupOwnerException("User is not a group owner");
        }
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
        if (input.hasPrivateGroup()){
            if (input.getPrivateGroup()){
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
        if (input.hasPassword() && !input.hasPrivateGroup()){
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
    public void joinGroup(Long groupId, JoinGroupRequestDto request, User user){
        if (groupId == null){
            log.warn("Fail - Group id is null");
            throw new AddUserRequestInvalidException("Group id is null");
        }
        log.info("User {} joining group {}", user.getUsername(), groupId);
        Group group = findGroupById(groupId);
        if (isUserInGroup(groupId, user)){
            log.warn("User {} is already in group {}", user.getUsername(), groupId);
            throw new UserAlreadyInGroupException("User is already in group");
        }
        if (group.isDeleted()){
            log.warn("Fail - Group {} is deleted", groupId);
            throw new GroupDeletedException("Group was deleted");
        }
        if (group.isPrivateGroup()){
            if (!passwordEncoder.matches(request.getPassword(), group.getPassword())){
                log.warn("Fail - Wrong password");
                throw new AddUserRequestInvalidException("Wrong password");
            }else{
                group.addMember(user);
            }
        }else{
            group.addMember(user);
        }
        groupRepository.save(group);
        log.debug("Success - User {} joined group {}", user.getUsername(), groupId);
    }

    @Transactional
    public void addUserToGroup(Long groupId, String username, User currentUser){
        if (!isUserGroupOwner(groupId, currentUser)){
            throw new UserNotGroupOwnerException("User is not group owner");
        }
        Group group = findGroupById(groupId);
        if (group.isDeleted()){
            log.warn("Fail - Group {} is deleted", groupId);
            throw new GroupDeletedException("Group was deleted");
        }
        User userToAdd = getUserByUsername(username);
        if (isUserInGroup(groupId, userToAdd)){
            log.warn("User {} is already in group {}", username, groupId);
            throw new UserAlreadyInGroupException("User is already in group");
        }
        group.addMember(userToAdd);
        groupRepository.save(group);
        log.debug("Success - User {} added to group {}", username, groupId);
    }

    @Transactional
    public void removeUserFromGroup(Long groupId, String username, User currentUser){
        if (!isUserGroupOwner(groupId, currentUser)){
            throw new UserNotGroupOwnerException("User is not a group owner");
        }
        log.info("Removing user {} from group {}", username, groupId);
        Group group = findGroupById(groupId);
        if (group.isDeleted()){
            log.warn("Fail - Group {} is deleted", groupId);
            throw new GroupDeletedException("Group was deleted");
        }
        User userToRemove = getUserByUsername(username);
        if (!isUserInGroup(groupId, userToRemove)){
            log.warn("User {} is not in group {}", username, groupId);
            throw new UserNotInGroupException("User is not in group");
        }
        group.removeMember(userToRemove);
        groupRepository.save(group);
        log.debug("Success - User {} removed from group {}", username, groupId);
    }


    @Transactional
    public void leaveGroup(Long groupId, User user){
        log.info("{} leaving group {}",user.getUsername(),  groupId);
        Group group = findGroupById(groupId);
        if (group.isDeleted()){
            log.warn("Fail - Group {} is deleted", groupId);
            throw new GroupDeletedException("Group was deleted");
        }
        if (!isUserInGroup(groupId, user)){
            log.warn("Fail - User {} is not in group {}", user.getUsername(), groupId);
            throw new UserNotInGroupException("User is not in group");
        }
        if (isUserGroupOwner(groupId, user)){
            log.warn("{} is the owner of group {}. Deleting group.", user.getUsername(), groupId);
            deleteGroupById(groupId, user);
            return;
        }
        group.removeMember(user);
        groupRepository.save(group);
        log.debug("Success - {} left group {}",user.getUsername(),  groupId);
    }


    private boolean isUserInGroup(Long groupId, User user){
        return groupRepository.existsByIdAndMembersId(groupId, user.getId()) || 
               groupRepository.existsByIdAndOwnerId(groupId, user.getId());
    }

    private boolean isUserGroupOwner(Long groupId, User user){
        return groupRepository.existsByIdAndOwnerId(groupId, user.getId());
    }

    private Group findGroupById(Long id){
        return groupRepository.findById(id).orElseThrow(()->{
            log.warn("Group with id: {} not found", id);
            return new GroupNotFoundException("Group not found");
        });
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("User with username: {} not found", username);
            return new UserNotFoundException("User not found");
        });
    }
}
