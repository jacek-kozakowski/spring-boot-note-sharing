package com.notex.student_notes.ai.translations.service;

import com.notex.student_notes.ai.translations.exceptions.TranslationException;
import com.notex.student_notes.ai.translations.language.Language;
import com.notex.student_notes.ai.translations.model.Translation;
import com.notex.student_notes.ai.translations.repository.TranslationRepository;
import com.notex.student_notes.note.exceptions.EmptyNoteException;
import com.notex.student_notes.note.exceptions.NoteDeletedException;
import com.notex.student_notes.note.exceptions.NoteNotFoundException;
import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TranslationService {

    private final TranslationRepository translationRepository;
    private final NoteRepository noteRepository;
    private final ChatClient chatClient;
    private static final String TRANSLATION_PROMPT =
        """
        You are a professional translator.
        Your task is to translate the following note from it's original language to %s.
        
        Follow these rules strictly:
        - The translation must be accurate and complete.
        - Translate whole text, not parts of it.
        - Maintain the original meaning and context.
        - Use proper grammar and vocabulary for %s.
        - Output only the translated text, nothing else.
        - If the note contains any illegal content, slurs, or anything non-educational,
          do not translate. Instead reply exactly with:
          "Sorry, I cannot translate this text. Reason: <explanation>"
          
        Note to translate:
        ---
        %s
        ---
        
        """;

    @Transactional
    @Cacheable(value = "translations", key = "T(String).valueOf(#noteId) + ':' + #language")
    public String translateNote(Long noteId, Language language){
        Optional<Translation> translation = translationRepository.findByNoteIdAndLanguage(noteId, language);
        if (translation.isPresent()){
            Translation existingTranslation = translation.get();
            if (existingTranslation.getCreatedAt().isAfter(existingTranslation.getNote().getUpdatedAt())){
                return existingTranslation.getTranslatedText();
            }else{
                translationRepository.delete(existingTranslation);
                log.info("Existing translation for note {} in language {} is outdated. Generating a new one.", noteId, language);
            }
        }
        Note note = getNoteIfValid(noteId);
        String translationText = getTranslation(note.getContent(), language);
        Translation newTranslation = new Translation();
        newTranslation.setNote(note);
        newTranslation.setLanguage(language);
        newTranslation.setTranslatedText(translationText);
        translationRepository.save(newTranslation);
        return translationText;
    }

    private String getTranslation(String noteText, Language language){
        String translation;
        try{
            String prompt = TRANSLATION_PROMPT.formatted(
                    language.getDisplayName(),
                    language.getDisplayName(),
                    noteText
            );
            translation = chatClient
                    .prompt()
                    .user(user -> user.text(prompt))
                    .call()
                    .content();
            if (translation == null || translation.isBlank()){
                throw new TranslationException("Empty translation response");
            }
            return translation;
        }catch (Exception e){
            log.warn("Fail - Failed to translate note {} to language {}", noteText, language);
            throw new TranslationException("Failed to translate note");
        }
    }

    private Note getNoteIfValid(Long noteId){
        Note note = noteRepository.findById(noteId).orElseThrow(()->{
            log.warn("Fail - Note {} does not exist.", noteId);
            return new NoteNotFoundException("Note not found");
        });
        if (note.getContent() == null || note.getContent().isBlank()){
            log.warn("Fail - Note {} is empty.", noteId);
            throw new EmptyNoteException("Note is empty");
        }
        if (note.isDeleted()){
            log.warn("Fail - Note {} is deleted", noteId);
            throw new NoteDeletedException("Note was deleted");
        }
        return note;
    }
}
