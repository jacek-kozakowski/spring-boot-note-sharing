package com.notex.student_notes.user.dto;

import com.notex.student_notes.user.model.Role;
import com.notex.student_notes.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminViewUserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean credentialsNonExpired;
    private boolean accountNonLocked;
    private boolean accountNonExpired;
    private Role role;
    private String verificationCode;
    private LocalDateTime verificationExpiration;

    public AdminViewUserDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getFirstName();
        this.enabled = user.isEnabled();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.accountNonExpired = user.isAccountNonExpired();
        this.role = user.getRole();
        this.verificationCode = user.getVerificationCode();
        this.verificationExpiration = user.getVerificationExpiration();
    }
}
