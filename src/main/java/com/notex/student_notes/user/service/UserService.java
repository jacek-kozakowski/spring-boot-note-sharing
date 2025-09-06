package com.notex.student_notes.user.service;

import com.notex.student_notes.auth.dto.NoChangesProvidedException;
import com.notex.student_notes.auth.exceptions.SamePasswordException;
import com.notex.student_notes.auth.exceptions.UserAlreadyExistsException;
import com.notex.student_notes.user.dto.AdminViewUserDto;
import com.notex.student_notes.user.dto.UpdateUserDto;
import com.notex.student_notes.user.dto.UserDto;
import com.notex.student_notes.user.exceptions.UserNotFoundException;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(()->{
            log.warn("User with Id: {} not found", id);
            return new UserNotFoundException("User not found");
        });
        return new UserDto(user);
    }

    public UserDto getUserByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow(()->{
            log.warn("User {} not found", username);
            return new UserNotFoundException("User not found");
        });
        return new UserDto(user);
    }
    public User getUserEntityByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(()->{
            log.warn("User {} not found", username);
            return new UserNotFoundException("User not found");
        });
    }

    public AdminViewUserDto getAdminViewUserByUsername(String username){
        log.info("Admin fetching user by username {}.", username);
        User user = userRepository.findByUsername(username).orElseThrow(()->{
            log.warn("User {} not found", username);
            return new UserNotFoundException("User not found");
        });
        log.debug("Success - Fetched user {}.", username);
        return new AdminViewUserDto(user);
    }

    public List<AdminViewUserDto> getAllAdminViewUser(){
        log.info("Admin fetching all users");
        List<AdminViewUserDto> users = userRepository.findAll().stream().map(AdminViewUserDto::new).toList();
        log.debug("Success - Fetched {} users", users.size());
        return users;
    }

    @Transactional
    public UserDto updateUser(String username, UpdateUserDto input ){
        log.info("Updating user {}", username);
        if (input == null || !input.hasAny()){
            log.warn("Fail - Can't update user {}: request is empty.", username);
            throw new NoChangesProvidedException("Empty request. Can't update user.");
        }
        User user = userRepository.findByUsername(username).orElseThrow(()->{
            log.warn("Fail - User {} not found.", username);
            return new UserNotFoundException("User not found");
        });
        if (input.hasUsername()){
            if(!userRepository.existsByUsername(input.getUsername())){
                user.setUsername(input.getUsername());
            }else{
                log.warn("Fail - Can't change current username {} to {}: username already in use.", user.getUsername(), input.getUsername());
                throw new UserAlreadyExistsException("Username is not available.");
            }
        }
        if (input.hasPassword() && passwordIsValid(input.getPassword(), user)){
            user.setPassword(passwordEncoder.encode(input.getPassword()));
        }
        if (input.hasEmail()){
            if(!userRepository.existsByEmail(input.getEmail())){
                user.setEmail(input.getEmail());
            }else{
                log.warn("Fail - Can't change current email {} to {}: email already in use.", user.getEmail(), input.getEmail());
                throw new UserAlreadyExistsException("Email is not available.");
            }
        }
        if (input.hasFirstName()){
            user.setFirstName(input.getFirstName());
        }
        if (input.hasLastName()){
            user.setLastName(input.getLastName());
        }
        User savedUser = userRepository.save(user);
        log.debug("Success - User {} updated successfully", username);
        return new UserDto(savedUser);
    }

    private boolean passwordIsValid(String password, User user) {
        if(passwordEncoder.matches(password, user.getPassword())){
            throw new SamePasswordException("Password matches already used password");
        }
        return true;
    }

}
