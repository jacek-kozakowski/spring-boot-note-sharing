package com.notex.student_notes.group.repository;

import com.notex.student_notes.group.model.Group;
import com.notex.student_notes.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);
    List<Group> findAllByOwner(User user);
    List<Group> findByNameContainingIgnoreCase(String namePart);

    boolean existsByName(String name);

    boolean existsByIdAndMembersId(Long groupId, Long userId);
    boolean existsByIdAndOwnerId(Long groupId, Long ownerId);
    boolean existsByIdAndMembersUsername(Long groupId, String username);
}
