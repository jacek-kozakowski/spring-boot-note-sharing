package com.notex.student_notes.summary.repository;

import com.notex.student_notes.summary.model.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Optional<Summary> findByNoteId(Long noteId);
}
