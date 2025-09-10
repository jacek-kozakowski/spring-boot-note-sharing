package com.notex.student_notes.note;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notex.student_notes.auth.security.JwtAuthFilter;
import com.notex.student_notes.note.controller.NoteController;
import com.notex.student_notes.note.dto.CreateNoteDto;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.dto.NoteImageDto;
import com.notex.student_notes.note.dto.UpdateNoteDto;
import com.notex.student_notes.note.mapper.NoteMapper;
import com.notex.student_notes.note.model.Note;
import com.notex.student_notes.note.repository.NoteRepository;
import com.notex.student_notes.note.service.NoteService;
import com.notex.student_notes.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = NoteController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class)
)
public class NoteControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

    @MockitoBean
    private NoteMapper noteMapper;

    @Autowired
    private ObjectMapper mapper;

    private NoteDto mockNoteDto;

    private User mockUser;

    @BeforeEach
    void setUp(){
        mockUser = new User();
        mockUser.setUsername("testuser");

        mockNoteDto = new NoteDto();
        mockNoteDto.setId(1L);
        mockNoteDto.setTitle("Test note");
        mockNoteDto.setContent("Test note content");
        mockNoteDto.setOwnerUsername("testuser");
        mockNoteDto.setCreatedAt(null);
        mockNoteDto.setUpdatedAt(null);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getNoteById_ShouldReturnNoteDto_WhenNoteExists() throws Exception {
        Long noteId = 1L;

        when(noteService.getNoteById(noteId)).thenReturn(mockNoteDto);

        mockMvc.perform(get("/notes/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test note"))
                .andExpect(jsonPath("$.content").value("Test note content"))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"));
    }


    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createNote_ShouldReturnNoteDto_WhenNoteCreated() throws Exception {

        CreateNoteDto input = new CreateNoteDto();
        input.setTitle("Test note");
        input.setContent("Test note content");

        MockMultipartFile mockFile = new MockMultipartFile(
                "images",
                "image1.jpg",
                "image/jpeg",
                "dummy-image-content".getBytes()
        );

        NoteDto response = new NoteDto();
        response.setId(1L);
        response.setTitle("Test note");
        response.setContent("Test note content");
        response.setOwnerUsername("testuser");
        response.setImages(List.of(
                new NoteImageDto(1L,0, "http://localhost:9000/bucket/1_0.image1.jpg")
        ));
        response.setCreatedAt(LocalDateTime.now());

        when(noteService.getUser(anyString())).thenReturn(mockUser);
        when(noteMapper.toDto(any(Note.class))).thenReturn(response);
        when(noteService.createNote(any(CreateNoteDto.class), eq(mockUser))).thenReturn(response);

        mockMvc.perform(multipart("/notes")
                        .file(mockFile)
                        .param("title", "Test note")
                        .param("content", "Test note content")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test note"))
                .andExpect(jsonPath("$.content").value("Test note content"))
                .andExpect(jsonPath("$.images[0].url").value("http://localhost:9000/bucket/1_0.image1.jpg"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.ownerUsername").value("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateNote_ShouldReturnNoteDto_WhenNoteUpdated() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        MockMultipartFile mockFile = new MockMultipartFile(
                "images",
                "image1.jpg",
                "image/jpeg",
                "dummy-image-content".getBytes()
        );

        NoteDto response = new NoteDto();
        response.setId(1L);
        response.setTitle("New note title");
        response.setContent("New note content");
        response.setOwnerUsername("testuser");
        response.setImages(List.of(
                new NoteImageDto(1L, 0, "http://localhost:9000/bucket/1_0.image1.jpg")
        ));
        response.setUpdatedAt(now);

        when(noteService.getUser(any())).thenReturn(mockUser);
        when(noteService.verifyUserIsOwner(anyLong(), any(User.class))).thenReturn(true);
        when(noteService.updateNote(anyLong(), any(UpdateNoteDto.class))).thenReturn(response);

        mockMvc.perform(multipart("/notes/1")
                        .file(mockFile)
                        .param("title", "New note title")
                        .param("content", "New note content")
                        .with(request -> {request.setMethod("PATCH"); return request;})
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New note title"))
                .andExpect(jsonPath("$.content").value("New note content"))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"))
                .andExpect(jsonPath("$.images[0].url").value("http://localhost:9000/bucket/1_0.image1.jpg"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        verify(noteService,times(1)).updateNote(anyLong(), any(UpdateNoteDto.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void updateNote_ShouldReturnForbidden_WhenUserIsNotOwner() throws Exception {
        when(noteService.getUser(any())).thenReturn(mockUser);
        when(noteService.verifyUserIsOwner(anyLong(), any(User.class))).thenReturn(false);

        mockMvc.perform(multipart("/notes/1")
                        .with(request -> {request.setMethod("PATCH"); return request;})
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteNote_ShouldReturnText_WhenNoteDeleted() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Note mockNote = new Note();
        mockNote.setId(1L);
        mockNote.setTitle("Test note");
        mockNote.setContent("Test note content");
        mockNote.setUpdatedAt(now);
        mockNote.setOwner(mockUser);

        when(noteService.getUser(any())).thenReturn(mockUser);
        when(noteService.verifyUserIsOwner(anyLong(), any(User.class))).thenReturn(true);
        mockMvc.perform(delete("/notes/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Note successfully deleted"));

        verify(noteService,times(1)).deleteNote(anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteNote_ShouldReturnForbidden_WhenUserIsNotOwner() throws Exception {
        when(noteService.getUser(any())).thenReturn(mockUser);
        when(noteService.verifyUserIsOwner(anyLong(), any(User.class))).thenReturn(false);

        mockMvc.perform(delete("/notes/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UpdateNoteDto())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteNoteImage_ShouldReturnText_WhenImageDeleted() throws Exception {
        when(noteService.getUser(any())).thenReturn(mockUser);
        when(noteService.verifyUserIsOwner(anyLong(), any(User.class))).thenReturn(true);

        mockMvc.perform(delete("/notes/1/images/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("Note image successfully deleted"));

        verify(noteService,times(1)).deleteNoteImage(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deleteNoteImage_ShouldReturnForbidden_WhenUserIsNotOwner() throws Exception {
        when(noteService.getUser(any())).thenReturn(mockUser);
        when(noteService.verifyUserIsOwner(anyLong(), any(User.class))).thenReturn(false);

        mockMvc.perform(delete("/notes/1/images/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

}
