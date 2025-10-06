package com.notex.student_notes.message.repository;

import com.notex.student_notes.message.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findAllByGroupIdOrderByCreatedAtAsc(Long groupId, Pageable pageable);
    List<Message> findByGroupIdAndCreatedAtAfterOrderByCreatedAtAsc(Long groupId, LocalDateTime since);
}

