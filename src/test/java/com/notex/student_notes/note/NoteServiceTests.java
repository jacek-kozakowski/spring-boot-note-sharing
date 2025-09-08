package com.notex.student_notes.note;


import com.notex.student_notes.auth.dto.NoChangesProvidedException;
import com.notex.student_notes.minio.service.MinioService;
import com.notex.student_notes.note.dto.CreateNoteDto;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.dto.NoteImageDto;
import com.notex.student_notes.note.dto.UpdateNoteDto;
import com.notex.student_notes.note.exceptions.NoteDeletedException;
import com.notex.student_notes.note.exceptions.NoteNotFoundException;
import com.notex.student_notes.note.mapper.NoteMapper;
import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.model.NoteImage;
import com.notex.student_notes.note.repository.NoteImageRepository;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.note.service.NoteService;
import com.notex.student_notes.user.model.User;
import com.notex.student_notes.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @InjectMocks
    private NoteService noteService;

    private static final String MOCK_CONTENT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    private static final String MOCK_TITLE = "TestTitle";

    private Note mockNote;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUsername("test");

        mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle(MOCK_TITLE);
        mockNote.setContent(MOCK_CONTENT);
        mockNote.setUpdatedAt(null);
        mockNote.setOwner(mockUser);
        mockNote.setDeleted(false);
        mockNote.setDeletedAt(null);
    }

    @Test
    void createNote_ShouldCreateNote_WhenDataIsValid() throws Exception {

        MultipartFile mockFile1 = mock(MultipartFile.class);
        when(mockFile1.getOriginalFilename()).thenReturn("image1.png");
        when(mockFile1.getInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));
        when(mockFile1.getSize()).thenReturn(5L);
        when(mockFile1.getContentType()).thenReturn("image/png");

        MultipartFile mockFile2 = mock(MultipartFile.class);
        when(mockFile2.getOriginalFilename()).thenReturn("image2.jpg");
        when(mockFile2.getInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));
        when(mockFile2.getSize()).thenReturn(5L);
        when(mockFile2.getContentType()).thenReturn("image/jpeg");

        CreateNoteDto input = new CreateNoteDto();
        input.setTitle(MOCK_TITLE);
        input.setContent(MOCK_CONTENT);
        input.setImages(List.of(mockFile1, mockFile2));

        NoteDto mockResponse = new NoteDto();
        mockResponse.setId(1L);
        mockResponse.setTitle(MOCK_TITLE);
        mockResponse.setContent(MOCK_CONTENT);
        mockResponse.setOwnerUsername("test");
        mockResponse.setImages(
                List.of(
                        new NoteImageDto(1L, 0, "http://localhost:9000/bucket/1_0.image1.png"),
                        new NoteImageDto(2L, 1, "http://localhost:9000/bucket/1_1.image2.jpg")

                )
        );
        when(noteRepository.save(any(Note.class))).thenAnswer(i -> i.getArgument(0));
        when(noteMapper.toDto(any(Note.class))).thenReturn(mockResponse);
        NoteDto response = noteService.createNote(input, mockUser);

        assertEquals(1L, response.getId());
        assertEquals(MOCK_TITLE, response.getTitle());
        assertEquals(MOCK_CONTENT, response.getContent());
        assertEquals("test", response.getOwnerUsername());
        assertEquals(2, response.getImages().size());
        assertEquals("http://localhost:9000/bucket/1_0.image1.png", response.getImages().get(0).getUrl());
        assertEquals("http://localhost:9000/bucket/1_1.image2.jpg", response.getImages().get(1).getUrl());

        verify(noteRepository, times(1)).save(any(Note.class));
        verify(minio, times(2)).uploadFile(anyString(), any(), anyLong(), anyString());
    }

    @Test
    void updateNote_ShouldUpdateNote_WhenDataIsValid() throws Exception{
        NoteImage existingImage = new NoteImage();
        existingImage.setId(1L);
        existingImage.setIndex(0);
        existingImage.setFilename("1_0.image1.png");

        mockNote.setImages(new ArrayList<>(List.of(existingImage)));

        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.getOriginalFilename()).thenReturn("image2.jpg");
        when(newFile.getInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));
        when(newFile.getSize()).thenReturn(5L);
        when(newFile.getContentType()).thenReturn("image/jpeg");

        UpdateNoteDto input = new UpdateNoteDto();
        input.setTitle("NewTitle");
        input.setContent("New note content");
        input.setNewImages(List.of(newFile));

        NoteDto mockResponse = new NoteDto();
        mockResponse.setId(1L);
        mockResponse.setTitle("NewTitle");
        mockResponse.setContent("New note content");
        mockResponse.setUpdatedAt(LocalDateTime.now());
        mockResponse.setOwnerUsername("test");
        mockResponse.setImages(List.of(
                new NoteImageDto(1L, 0, "http://localhost:9000/bucket/1_0.image1.png"),
                new NoteImageDto(2L, 1, "http://localhost:9000/bucket/1_1.image2.jpg")
        ));

        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));
        when(noteMapper.toDto(any(Note.class))).thenReturn(mockResponse);
        when(noteRepository.save(any(Note.class))).thenAnswer(i -> i.getArgument(0));

        NoteDto response = noteService.updateNote(1L, input);

        assertEquals("NewTitle", response.getTitle());
        assertEquals("New note content", response.getContent());
        assertEquals("test", response.getOwnerUsername());
        assertEquals(1L, response.getId());
        assertNotNull(response.getUpdatedAt());
        assertEquals("http://localhost:9000/bucket/1_0.image1.png", response.getImages().get(0).getUrl());
        assertEquals("http://localhost:9000/bucket/1_1.image2.jpg", response.getImages().get(1).getUrl());

        verify(noteRepository, times(1)).findById(1L);
        verify(noteRepository, times(1)).save(any(Note.class));
        verify(minio, times(1)).uploadFile(anyString(), any(), anyLong(), anyString());
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
    void deleteNote_ShouldDeleteNote_WhenNoteFound() throws Exception {
        NoteImage existingImage = new NoteImage();
        existingImage.setId(1L);
        existingImage.setIndex(0);
        existingImage.setFilename("1_0.image1.png");
        existingImage.setNote(mockNote);

        mockNote.setImages(new ArrayList<>(List.of(existingImage)));
        mockNote.setDeleted(false);
        mockNote.setDeletedAt(null);

        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));
        when(noteRepository.save(any(Note.class))).thenAnswer(i->i.getArgument(0));

        noteService.deleteNote(1L);

        assertTrue(mockNote.isDeleted());
        assertNotNull(mockNote.getDeletedAt());

        verify(noteRepository, times(1)).findById(1L);
        verify(noteRepository, times(1)).save(any(Note.class));
        verify(minio, times(1)).deleteFile(anyString());
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
    void deleteNote_ShouldThrowException_WhenNoteAlreadyDeleted(){
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

    @Test
    void deleteNoteImage_ShouldDeleteImage_WhenImageFound(){
        NoteImage existingImage = new NoteImage();
        existingImage.setId(1L);
        existingImage.setIndex(0);
        existingImage.setFilename("1_0.image1.png");
        existingImage.setNote(mockNote);

        mockNote.setImages(new ArrayList<>(List.of(existingImage)));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(mockNote));
        noteService.deleteNoteImage(1L, 1L);


        assertEquals(0, mockNote.getImages().size());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

}
