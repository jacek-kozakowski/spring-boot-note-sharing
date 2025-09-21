package com.notex.student_notes.ai.translations.controller;

import com.notex.student_notes.ai.translations.language.Language;
import com.notex.student_notes.ai.translations.service.TranslationService;
import com.notex.student_notes.group.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/notes/{noteId}/translate")
public class TranslationController {

    private final TranslationService translationService;

    @GetMapping
    public ResponseEntity<ApiResponse> translateNote(@PathVariable Long noteId, @RequestParam(required = true) Language language){
        log.info("GET /notes/{}/translate: Translating note.", noteId);
        String translatedText = translationService.translateNote(noteId, language);
        return ResponseEntity.ok(new ApiResponse(translatedText));
    }
}
