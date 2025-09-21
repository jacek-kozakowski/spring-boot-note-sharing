package com.notex.student_notes.ai.summary.repository;

import com.notex.student_notes.ai.summary.model.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Optional<Summary> findByNoteId(Long noteId);
}
