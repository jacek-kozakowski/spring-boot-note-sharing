package com.notex.student_notes.config.ai;

import com.notex.student_notes.config.ratelimiting.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiCallsLimitingService {

    private final Map<String, List<LocalDateTime>> aiCalls = new ConcurrentHashMap<>();
    private static final int MAX_CALLS_PER_MINUTE = 3;

    public void checkAiCalls(String address){
        if (address == null){
            throw new IllegalArgumentException("Address must not be null");
        }
        List<LocalDateTime> timestamps = aiCalls.computeIfAbsent(address, k -> new ArrayList<>());
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(1);

        timestamps.removeIf(time -> time.isBefore(cutoff));

        if (timestamps.size() >= MAX_CALLS_PER_MINUTE){
            throw new RateLimitExceededException("AI call limit exceeded: " + MAX_CALLS_PER_MINUTE + " calls per minute");
        }
        timestamps.add(LocalDateTime.now());
    }
}
