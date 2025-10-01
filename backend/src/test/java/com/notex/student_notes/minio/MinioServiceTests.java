package com.notex.student_notes.minio;

import com.notex.student_notes.minio.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MinioServiceTests {

    @Autowired
    private MinioService minioService;

    @Test
    void uploadFile_ShouldReturnFilenameWithExtension() throws Exception {
        String key = "test.txt";
        String content = "Hello MinIO";
        InputStream is = new ByteArrayInputStream(content.getBytes());

        String filename = minioService.uploadFile(key, is, content.length(), "text/plain");

        assertNotNull(filename);
        assertTrue(filename.endsWith(".txt"));
        assertTrue(filename.length() > 10);
    }

    @Test
    void getFileUrl_ShouldReturnValidUrl() {
        String filename = "test-file.txt";

        String url = minioService.getFileUrl(filename);

        assertNotNull(url);
        assertTrue(url.contains("localhost:9000"));
        assertTrue(url.contains("notex-notes"));
        assertTrue(url.contains(filename));
    }

    @Test
    void isHealthy_ShouldReturnTrue() {
        boolean isHealthy = minioService.isHealthy();

        assertTrue(isHealthy);
    }

    @Test
    void getBucketName_ShouldReturnCorrectBucket() {
        String bucketName = minioService.getBucketName();

        assertEquals("notex-notes", bucketName);
    }
}