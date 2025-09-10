package com.notex.student_notes.group.dto;


import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UpdateGroupDto {

    @Size(min = 3, max = 50)
    private String name;

    @Size(max = 5000)
    private String description;

    private Boolean privateGroup;

    private String password;


    public boolean hasName(){
        return name != null && !name.isBlank();
    }
    public boolean hasDescription(){
        return description != null && !description.isBlank();
    }
    public boolean hasPrivateGroup(){
        return privateGroup != null;
    }

    public boolean hasPassword(){
        return password != null && !password.isBlank();
    }

    public boolean hasAny(){
        return hasName() || hasDescription() || hasPrivateGroup() || hasPassword();
    }
}
