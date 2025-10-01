package com.notex.student_notes.ai.summary.repository;

import com.notex.student_notes.ai.summary.model.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    Optional<Summary> findByNoteId(Long noteId);

    @Transactional
    @Modifying
    void deleteByNoteId(Long id);
}
