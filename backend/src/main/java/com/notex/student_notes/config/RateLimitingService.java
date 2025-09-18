package com.notex.student_notes.config;

import com.notex.student_notes.config.exceptions.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingService {

    private final Map<String, List<LocalDateTime>> requestHistory = new ConcurrentHashMap<>();

    public void checkRateLimit(String key, int limit, int windowMinutes){
        List<LocalDateTime> timestamps = requestHistory.computeIfAbsent(key, k -> new ArrayList<>());
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(windowMinutes);
        
        timestamps.removeIf(time -> time.isBefore(cutoff));

        if (timestamps.size() >= limit){
            throw new RateLimitExceededException("Rate limit exceeded: " + limit + " requests per " + windowMinutes + " minute(s)");
        }
        timestamps.add(LocalDateTime.now());
    }
}
