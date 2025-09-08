package com.notex.student_notes.chat.repository;

import com.notex.student_notes.chat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByGroupIdOrderByCreatedAtAsc(Long groupId);
    List<Message> findByGroupIdAndCreatedAtAfterOrderByCreatedAtAsc(Long groupId, LocalDateTime since);
}

