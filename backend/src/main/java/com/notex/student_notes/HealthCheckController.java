package com.notex.student_notes;

import jakarta.servlet.http.HttpServletRequest;
import com.notex.student_notes.config.RateLimitingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {
    private final RateLimitingService rateLimitingService;

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health/rate-limiting")
    public ResponseEntity<String> rateLimitingHealthCheck(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        try {
            rateLimitingService.checkRateLimit(remoteAddress, 0, 1);
            return ResponseEntity.ok("OK");
        } catch (com.notex.student_notes.config.exceptions.RateLimitExceededException e) {
            return ResponseEntity.status(429).body("Rate limiting is enabled");
        }
    }
}
