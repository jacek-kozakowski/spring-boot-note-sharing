package com.notex.student_notes.ai.translations.repository;

import com.notex.student_notes.ai.translations.language.Language;
import com.notex.student_notes.ai.translations.model.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {

    Optional<Translation> findByNoteId(Long noteId);
    Optional<Translation> findByNoteIdAndLanguage(Long noteId, Language language);
}
