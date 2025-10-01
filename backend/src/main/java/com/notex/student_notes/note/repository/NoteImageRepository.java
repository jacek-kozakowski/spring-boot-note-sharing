package com.notex.student_notes.note.repository;

import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.model.NoteImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteImageRepository extends JpaRepository<NoteImage, Long> {
    void deleteByNote(Note note);
    List<NoteImage> findAllByNote(Note note);
}
