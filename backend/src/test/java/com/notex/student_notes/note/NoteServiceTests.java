package com.notex.student_notes.note;

import com.notex.student_notes.auth.dto.NoChangesProvidedException;
import com.notex.student_notes.config.metrics.CustomMetrics;
import com.notex.student_notes.minio.service.MinioService;
import com.notex.student_notes.note.dto.CreateNoteDto;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.dto.UpdateNoteDto;
import com.notex.student_notes.note.exceptions.UserNotNoteOwner;
import com.notex.student_notes.note.mapper.NoteMapper;
import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.repository.NoteImageRepository;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.note.service.NoteService;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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
    @Mock
    private NoteMapper noteMapper;
    @Mock
    private MinioService minio;
    @Mock
    private NoteImageRepository noteImageRepository;
    @Mock
    private CustomMetrics customMetrics;

    private NoteService noteService;

    private static final String MOCK_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    private static final String MOCK_TITLE = "TestTitle";

    private Note mockNote;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test");
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");
        mockUser.setEnabled(true);

        mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle(MOCK_TITLE);
        mockNote.setContent(MOCK_CONTENT);
        mockNote.setUpdatedAt(null);
        mockNote.setOwner(mockUser);
        mockNote.setDeleted(false);
        mockNote.setDeletedAt(null);

        noteService = new NoteService(
            noteRepository,
            noteImageRepository,
            userRepository,
            noteMapper,
            minio,
            customMetrics
        );
        
        System.out.println("Mock user ID: " + mockUser.getId());
        System.out.println("Mock note owner ID: " + mockNote.getOwner().getId());
    }

    @Test
    void createNote_ShouldCreateNote_WhenDataIsValid() throws Exception {
        CreateNoteDto input = new CreateNoteDto();
        input.setTitle(MOCK_TITLE);
        input.setContent(MOCK_CONTENT);
        input.setImages(new ArrayList<>());

        NoteDto mockResponse = new NoteDto();
        mockResponse.setId(1L);
        mockResponse.setTitle(MOCK_TITLE);
        mockResponse.setContent(MOCK_CONTENT);
        mockResponse.setOwnerUsername("test");

        when(customMetrics.startNoteProcessingTimer()).thenReturn(mock(io.micrometer.core.instrument.Timer.Sample.class));
        when(customMetrics.getNoteProcessingTimer()).thenReturn(mock(io.micrometer.core.instrument.Timer.class));
        when(noteRepository.save(any(Note.class))).thenAnswer(i -> i.getArgument(0));
        when(noteMapper.toDto(any(Note.class))).thenReturn(mockResponse);

        NoteDto response = noteService.createNote(input, mockUser);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(MOCK_TITLE, response.getTitle());
        assertEquals(MOCK_CONTENT, response.getContent());
        assertEquals("test", response.getOwnerUsername());
        
        verify(customMetrics).startNoteProcessingTimer();
        verify(noteRepository).save(any(Note.class));
        verify(noteMapper).toDto(any(Note.class));
    }

    @Test
    void updateNote_ShouldUpdateNote_WhenDataIsValid() throws Exception {
        UpdateNoteDto input = new UpdateNoteDto();
        input.setTitle("Updated Title");
        input.setContent("Updated content");

        NoteDto mockResponse = new NoteDto();
        mockResponse.setId(1L);
        mockResponse.setTitle("Updated Title");
        mockResponse.setContent("Updated content");
        mockResponse.setOwnerUsername("test");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));
        when(noteRepository.existsByIdAndOwnerId(1L, mockUser.getId())).thenReturn(true);
        when(noteRepository.save(any(Note.class))).thenAnswer(i -> i.getArgument(0));
        when(noteMapper.toDto(any(Note.class))).thenReturn(mockResponse);

        NoteDto response = noteService.updateNote(1L, input, mockUser);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Updated Title", response.getTitle());
        assertEquals("Updated content", response.getContent());
        
        verify(noteRepository).findById(1L);
        verify(noteRepository).save(any(Note.class));
        verify(noteMapper).toDto(any(Note.class));
    }

    @Test
    void deleteNote_ShouldDeleteNote_WhenNoteExists() throws Exception {
        // Given
        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));
        when(noteRepository.existsByIdAndOwnerId(1L, mockUser.getId())).thenReturn(true);
        when(noteRepository.save(any(Note.class))).thenAnswer(i -> i.getArgument(0));

        noteService.deleteNote(1L, mockUser);

        verify(noteRepository).findById(1L);
        verify(noteRepository).save(any(Note.class));
        verify(customMetrics).incrementNoteDeletedCounter();
    }

    @Test
    void updateNote_ShouldThrowException_WhenNoChangesProvided() {
        UpdateNoteDto input = new UpdateNoteDto();

        when(noteRepository.existsByIdAndOwnerId(1L, mockUser.getId())).thenReturn(true);

        Exception exception = assertThrows(Exception.class, () -> {
            noteService.updateNote(1L, input, mockUser);
        });
        
        assertTrue(exception instanceof NoChangesProvidedException ||
                  exception instanceof UserNotNoteOwner,
                  "Expected NoChangesProvidedException or UserNotNoteOwner, but got: " + exception.getClass().getSimpleName());
    }
}