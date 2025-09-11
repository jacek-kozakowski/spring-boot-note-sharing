package com.notex.student_notes.note.mapper;

import com.notex.student_notes.minio.service.MinioService;
import com.notex.student_notes.note.dto.NoteDto;
import com.notex.student_notes.note.dto.NoteImageDto;
import com.notex.student_notes.note.model.Note;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NoteMapper {
    private final MinioService minioService;

    public NoteDto toDto(Note note){
        NoteDto noteDto = new NoteDto();
        noteDto.setId(note.getId());
        noteDto.setTitle(note.getTitle());
        noteDto.setContent(note.getContent());
        noteDto.setOwnerUsername(note.getOwner().getUsername());
        noteDto.setCreatedAt(note.getCreatedAt());
        noteDto.setUpdatedAt(note.getUpdatedAt());

        noteDto.setImages(note.getImages().stream().map(
                img ->{
                    NoteImageDto noteImage = new NoteImageDto();
                    noteImage.setId(img.getId());
                    noteImage.setIndex(img.getIndex());
                    noteImage.setUrl(minioService.getFileUrl(img.getFilename()));
                    return noteImage;
                })
                .toList()
        );
        return noteDto;
    }
}
