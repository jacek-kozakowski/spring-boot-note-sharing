package com.notex.student_notes.group.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private int membersCount;
    private LocalDateTime createdAt;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    @JsonProperty("isMember")
    private boolean isMember;

    public GroupDto(Group group){
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.ownerUsername = group.getOwner().getUsername();
        this.membersCount = group.getMembers().size();
        this.createdAt = group.getCreatedAt();
        this.isPrivate = group.isPrivate();
        this.isMember = false;
    }

    public GroupDto(Group group, boolean isMember){
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.ownerUsername = group.getOwner().getUsername();
        this.membersCount = group.getMembers().size();
        this.createdAt = group.getCreatedAt();
        this.isPrivate = group.isPrivate();
        this.isMember = isMember;
    }
}
