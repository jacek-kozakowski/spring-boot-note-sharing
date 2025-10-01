package com.notex.student_notes.ai.translations.repository;

import com.notex.student_notes.ai.translations.language.Language;
import com.notex.student_notes.ai.translations.model.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {

    Optional<Translation> findByNoteId(Long noteId);
    Optional<Translation> findByNoteIdAndLanguage(Long noteId, Language language);

    @Transactional
    @Modifying
    void deleteByNoteIdAndLanguage(Long noteId, Language language);
}
