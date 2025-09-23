package com.notex.student_notes.minio;

import com.notex.student_notes.minio.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class MinioServiceTests {

    @Autowired
    private MinioService minioService;

    @Test
    void uploadFile() throws Exception {
        String key = "test.txt";
        String content = "Hello MinIO";
        InputStream is = new ByteArrayInputStream(content.getBytes());

        String url = minioService.uploadFile(key, is, content.length(), "text/plain");

        // Just test that upload returns a URL
        assertEquals(true, url.contains("test.txt"));
    }
}
