package com.notex.student_notes.group.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddUserToGroupRequest {
    private Long groupId;
    private String password;
}
