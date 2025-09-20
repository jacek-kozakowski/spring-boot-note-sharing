package com.notex.student_notes.summary.service;

import com.notex.student_notes.summary.exceptions.SummaryGenerationFailedException;
import com.notex.student_notes.summary.model.Summary;
import com.notex.student_notes.summary.repository.SummaryRepository;
import com.notex.student_notes.note.exceptions.EmptyNoteException;
import com.notex.student_notes.note.exceptions.NoteDeletedException;
import com.notex.student_notes.note.exceptions.NoteNotFoundException;
import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.model.NoteImage;
import com.notex.student_notes.note.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private final ChatClient chatClient;
    private final NoteRepository noteRepository;
    private final SummaryRepository summaryRepository;

    private static final String SUMMARY_PROMPT = """
        You are a professional university teacher.
        Summarize the following note clearly and comprehensively in English,
        so that it is easy to understand for a university student.
    
        Follow these rules strictly:
        - The summary must focus only on the main points.
        - Do not include every detail, only the essential ideas.
        - For longer texts the summary must be about 20%% of the original text length .
        - For shorter texts the summary must be about 3 sentences. If needed add more sentences - max 5 sentences in total.
        - Output only the summary text, nothing else.

        If the note contains illegal content, slurs, or anything non-educational,
        do not summarize. Instead reply exactly with:
        "Sorry, I cannot summarize this text. Reason: <explanation>"

        Note:
        ---
        %s
        ---
        """;

    @Transactional
    public String summarizeNote(Long id){
        log.info("Summarizing note {}", id);
        Optional<Summary> summary = summaryRepository.findByNoteId(id);
        if(summary.isPresent()){
            Summary existingSummary = summary.get();
            if(existingSummary.getExpiresAt().isBefore(LocalDateTime.now())){
                log.info("Note {} was not summarized in the last 24 hours. Re-summarizing.", id);
                summaryRepository.delete(existingSummary);
            }else{
                log.debug("Success - Note {} already summarized", id);
                return existingSummary.getText();
            }
        }

        Note note = getNoteIfValid(id);
        String newSummaryText = generateSummary(note);
        Summary newSummary = new Summary();
        newSummary.setNote(note);
        newSummary.setText(newSummaryText);
        summaryRepository.save(newSummary);
        log.info("Note {} summarized successfully.", id);
        return newSummaryText;
    }

    private Note getNoteIfValid(Long id){
        Note note = noteRepository.findById(id).orElseThrow(()->{
            log.warn("Fail - Note {} does not exist.", id);
            return new NoteNotFoundException("Note not found");
        });
        if (note.getContent() == null || note.getContent().isBlank()){
            log.warn("Fail - Note {} is empty.", id);
            throw new EmptyNoteException("Note is empty");
        }
        if (note.isDeleted()){
            log.warn("Fail - Note {} is deleted", id);
            throw new NoteDeletedException("Note was deleted");
        }
        return note;
    }

    private String generateSummary(Note note){
        String newSummaryText;
        List<NoteImage> noteImages = note.getImages();
        try {
            newSummaryText = chatClient
                    .prompt()
                    .user(userSpec ->{
                        userSpec.text(String.format(SUMMARY_PROMPT, note.getContent()));

                        if(!noteImages.isEmpty()){
                            attachImagesToUserSpec(userSpec, noteImages);
                        }
                    })
                    .call()
                    .content();
        }catch (Exception e){
            log.error("Error - Failed to summarize note {}", note.getId(), e);
            throw new SummaryGenerationFailedException("Failed to summarize note");
        }
        return newSummaryText;
    }

    private void attachImagesToUserSpec(ChatClient.PromptUserSpec userSpec, List<NoteImage> noteImages){
        for (NoteImage noteImage : noteImages) {
            try {
                userSpec.media(noteImage.getMedia());
            } catch (MalformedURLException e) {
                log.error("Error - Failed to attach image {} to user spec", noteImage.getId(), e);
                throw new SummaryGenerationFailedException("Failed to generate summary - Malformed URL");
            }
        }
    }
}
