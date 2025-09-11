package com.notex.student_notes.note.repository;

import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findById(Long id);
    List<Note> findAllByOwner(User user);
    List<Note> findAllByTitleContainingIgnoreCase(String title);
    boolean existsByOwner(User user);
}
