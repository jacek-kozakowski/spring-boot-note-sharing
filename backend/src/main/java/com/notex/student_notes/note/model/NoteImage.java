package com.notex.student_notes.note.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.ai.model.Media;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "note_images")
@NoArgsConstructor
public class NoteImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Column(nullable = false, length = 500)
    private String filename;

    @Transient
    private String url;


    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Media getMedia() throws MalformedURLException {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        MediaType mediaType = switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
        URI uri = URI.create(this.getUrl());
        URL url = uri.toURL();

        org.springframework.util.MimeType mime = MimeTypeUtils.parseMimeType(mediaType.toString());
        return new Media(mime, url);
    }

}
