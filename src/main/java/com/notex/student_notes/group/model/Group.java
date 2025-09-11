package com.notex.student_notes.group.model;

import com.notex.student_notes.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "groups")
public class Group {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    @Size(min=3, max=50)
    private String name;

    @Column(nullable = false)
    @Size(max=5000)
    private String description;

    @Column(nullable = false)
    private boolean privateGroup;

    @Size(min =8)
    private String password;

    private boolean deleted;
    private LocalDateTime deletedAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private LocalDateTime createdAt;


    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.deleted = false;
        this.deletedAt = null;
        this.updatedAt = null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setOwner(User user) {
        this.owner = user;
        this.addMember(user);
    }
    public void addMember(User user) {
        members.add(user);
    }
    public void removeMember(User user) {
        members.remove(user);
    }
}
