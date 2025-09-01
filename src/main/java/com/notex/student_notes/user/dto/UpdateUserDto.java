package com.notex.student_notes.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserDto {
    @Size(min = 3, max = 50)
    private String username;
    @Size(min = 8)
    private String password;
    @Email
    private String email;
    @Size(max = 100)
    private String firstName;
    @Size(max = 100)
    private String lastName;

    public boolean hasUsername() {
        return username != null && !username.isBlank();
    }

    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }

    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }

    public boolean hasFirstName() {
        return firstName != null && !firstName.isBlank();
    }

    public boolean hasLastName() {
        return lastName != null && !lastName.isBlank();
    }

    public boolean hasAny(){
        return hasUsername() || hasEmail() || hasPassword() || hasFirstName() || hasLastName();
    }

}
