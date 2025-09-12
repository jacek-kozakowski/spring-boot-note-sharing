package com.notex.student_notes.user.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.notex.student_notes.user.model.Role;
import com.notex.student_notes.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonPropertyOrder({"id", "username", "email", "firstName", "lastName", "role", "enabled"})
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean enabled;

    public UserDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
    }
}
