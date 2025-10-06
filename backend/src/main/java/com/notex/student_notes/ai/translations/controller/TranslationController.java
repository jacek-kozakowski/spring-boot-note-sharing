package com.notex.student_notes.ai.translations.controller;

import com.notex.student_notes.ai.translations.language.Language;
import com.notex.student_notes.ai.translations.service.TranslationService;
import com.notex.student_notes.config.ratelimiting.RateLimitingService;
import com.notex.student_notes.group.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
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
    private final RateLimitingService rateLimitingService;

    @GetMapping
    public ResponseEntity<ApiResponse> translateNote(@PathVariable Long noteId, @RequestParam(required = true) Language language, HttpServletRequest request){
        String remoteAddress = request.getRemoteAddr();
        log.info("GET /notes/{}/translate: Translating note.", noteId);
        rateLimitingService.checkRateLimit(remoteAddress, "/notes/{noteId}/translate", 5, 1);
        String translatedText = translationService.translateNote(noteId, language, remoteAddress);
        return ResponseEntity.ok(new ApiResponse(translatedText));
    }
}
