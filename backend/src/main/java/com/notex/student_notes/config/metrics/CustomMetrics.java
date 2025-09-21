package com.notex.student_notes.config.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CustomMetrics {
    private final Counter noteCreatedCounter;
    private final Counter noteUpdatedCounter;
    private final Counter noteDeletedCounter;
    private final Timer noteProcessingTimer;

    public CustomMetrics(MeterRegistry meterRegistry){
        this.noteCreatedCounter = Counter.builder("note.created")
                .description("Number of notes created")
                .register(meterRegistry);

        this.noteDeletedCounter = Counter.builder("note.deleted")
                .description("Number of notes deleted")
                .register(meterRegistry);
        this.noteUpdatedCounter = Counter.builder("note.updated")
                .description("Number of notes updated")
                .register(meterRegistry);
        this.noteProcessingTimer = Timer.builder("note.processing.time")
                .description("Time spent on note processing")
                .register(meterRegistry);
    }

    public void incrementNoteCreatedCounter(){
        noteCreatedCounter.increment();
    }
    public void incrementNoteUpdatedCounter(){
        noteUpdatedCounter.increment();
    }

    public void incrementNoteDeletedCounter(){
        noteDeletedCounter.increment();
    }
    public Timer.Sample startNoteProcessingTimer(){
        return Timer.start();
    }
}
