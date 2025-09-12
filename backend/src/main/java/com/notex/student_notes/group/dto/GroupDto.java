package com.notex.student_notes.group.dto;

import com.notex.student_notes.group.model.Group;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GroupDto {
    private Long id;
    private String name;
    private String description;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private boolean isPrivate;

    public GroupDto(Group group){
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.ownerUsername = group.getOwner().getUsername();
        this.createdAt = group.getCreatedAt();
        this.isPrivate = group.isPrivateGroup();
    }
}
