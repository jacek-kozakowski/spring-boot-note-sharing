package com.notex.student_notes.note;


import com.notex.student_notes.auth.dto.NoChangesProvidedException;
import com.notex.student_notes.note.dto.CreateNoteDto;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.dto.UpdateNoteDto;
import com.notex.student_notes.note.exceptions.NoteDeletedException;
import com.notex.student_notes.note.exceptions.NoteNotFoundException;
import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.note.service.NoteService;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    private static final String MOCK_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    private static final String MOCK_TITLE = "TestTitle";
    @Test
    void createNote_ShouldCreateNote_WhenDataIsValid(){
        User mockUser = new User();
        mockUser.setUsername("test");

        CreateNoteDto input = new CreateNoteDto();
        input.setTitle(MOCK_TITLE);
        input.setContent(MOCK_CONTENT);

        when(noteRepository.save(any(Note.class))).thenAnswer(i -> i.getArgument(0));

        NoteDto response = noteService.createNote(input, mockUser);

        assertEquals(MOCK_TITLE, response.getTitle());
        assertEquals(MOCK_CONTENT, response.getContent());
        assertEquals("test", response.getOwnerUsername());

        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void updateNote_ShouldUpdateNote_WhenDataIsValid(){
        User mockUser = new User();
        mockUser.setUsername("test");

        UpdateNoteDto input = new UpdateNoteDto();
        input.setTitle("NewTitle");
        input.setContent("New note content");

        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle(MOCK_TITLE);
        mockNote.setContent(MOCK_CONTENT);
        mockNote.setUpdatedAt(null);
        mockNote.setOwner(mockUser);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(i -> i.getArgument(0));

        NoteDto response = noteService.updateNote(1L, input);

        assertEquals("NewTitle", response.getTitle());
        assertEquals("New note content", response.getContent());
        assertEquals("test", response.getOwnerUsername());
        assertEquals(1L, response.getId());
        assertNotNull(response.getUpdatedAt());

        verify(noteRepository, times(1)).findById(1L);
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenNoteNotFound(){
        UpdateNoteDto input = new UpdateNoteDto();
        input.setTitle("NewTitle");
        input.setContent("New note content");

        when(noteRepository.findById(any())).thenReturn(Optional.empty());
        NoteNotFoundException ex = assertThrows(NoteNotFoundException.class, ()-> noteService.updateNote(1L, input));

        assertEquals("Note not found", ex.getMessage());

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUpdateBodyIsEmpty(){
        UpdateNoteDto input = new UpdateNoteDto();

        NoChangesProvidedException ex = assertThrows(NoChangesProvidedException.class, ()->noteService.updateNote(1L, input));

        assertEquals("Empty request. Can't update note.", ex.getMessage());

        verify(noteRepository, never()).findById(any());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void deleteNote_ShouldDeleteNote_WhenNoteFound(){
        User mockUser = new User();
        mockUser.setUsername("test");

        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle(MOCK_TITLE);
        mockNote.setContent(MOCK_CONTENT);
        mockNote.setUpdatedAt(null);
        mockNote.setOwner(mockUser);
        mockNote.setDeleted(false);
        mockNote.setDeletedAt(null);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(i->i.getArgument(0));

        noteService.deleteNote(1L);

        assertTrue(mockNote.isDeleted());
        assertNotNull(mockNote.getDeletedAt());

        verify(noteRepository, times(1)).findById(1L);
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void deleteNote_ShouldThrowException_WhenNoteNotFound(){
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());
        NoteNotFoundException ex = assertThrows(NoteNotFoundException.class, ()-> noteService.deleteNote(1L));

        assertEquals("Note not found", ex.getMessage());

        verify(noteRepository, times(1)).findById(1L);
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void deleteNote_ShouldThrowException_WhenNoteDeleted(){
        User mockUser = new User();
        mockUser.setUsername("test");

        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle(MOCK_TITLE);
        mockNote.setContent(MOCK_CONTENT);
        mockNote.setUpdatedAt(null);
        mockNote.setOwner(mockUser);
        mockNote.setDeleted(true);
        mockNote.setDeletedAt(LocalDateTime.now());

        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));
        NoteDeletedException ex = assertThrows(NoteDeletedException.class, ()->noteService.deleteNote(1L));

        assertEquals("Note was deleted", ex.getMessage());
        assertTrue(mockNote.isDeleted());

        verify(noteRepository, times(1)).findById(1L);
        verify(noteRepository, never()).save(any(Note.class));
    }

}
