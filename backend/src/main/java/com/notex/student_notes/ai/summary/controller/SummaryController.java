package com.notex.student_notes.ai.summary.controller;

import com.notex.student_notes.ai.summary.service.SummaryService;
import com.notex.student_notes.config.RateLimitingService;
import com.notex.student_notes.group.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notes/{noteId}/summarize")
@RequiredArgsConstructor
public class SummaryController {
    private final SummaryService summaryService;
    private final RateLimitingService rateLimitingService;

    @GetMapping
    public ResponseEntity<ApiResponse> summarizeNote(@PathVariable Long noteId, HttpServletRequest request){
        String remoteAddress = request.getRemoteAddr();
        rateLimitingService.checkRateLimit(remoteAddress,"/notes/{noteId}/summarize", 5, 1);
        return ResponseEntity.ok(new ApiResponse(summaryService.summarizeNote(noteId)));
    }
}
