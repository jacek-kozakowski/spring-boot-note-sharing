package com.notex.student_notes.minio.service;

import com.notex.student_notes.note.exceptions.NoteImageUploadException;
import io.minio.*;
import io.minio.http.Method;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Service
@Getter
@Slf4j
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName;
    private final String url;
    private final Map<String, Set<String>> ALLOWED_TYPES = Map.of(
            ".png", Set.of("image/png"),
            ".jpg", Set.of("image/jpeg"),
            ".jpeg", Set.of("image/jpeg"),
            ".pdf", Set.of("application/pdf"),
            ".pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            ".docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            ".txt", Set.of("text/plain")
    );
    private static final long MAX_FILE_SIZE = 1024 * 1024 * 10;


    public MinioService(@Value("${minio.url}") String url,
                        @Value("${minio.access-key}") String accessKey,
                        @Value("${minio.secret-key}") String secretKey,
                        @Value("${minio.bucket-name}") String bucketName
    ) throws Exception {
        this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
        this.bucketName = bucketName;
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    public String uploadFile(String fileName, InputStream inputStream, long size, String contentType) throws Exception{
        log.info("Uploading file {} to bucket {}", fileName, bucketName);

        if (size > MAX_FILE_SIZE) {
            log.warn("Upload rejected - File size too large.");
            throw new NoteImageUploadException("Upload rejected. Max file size: " + MAX_FILE_SIZE / 1024 / 1024 + "MB");
        }

        if (!isAllowed(fileName, contentType)) {
            log.warn("Upload rejected - File extension not allowed.");
            throw new NoteImageUploadException("Upload rejected. Allowed extensions:" + String.join(",", ALLOWED_TYPES.keySet()));
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, size, -1)
                        .contentType(contentType)
                        .build()
        );
        return fileName;
    }

    public InputStream getFile(String filename) throws Exception{
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build()
        );
    }
    public void deleteFile(String filename) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .build()
        );
    }

    private boolean isAllowed(String filename, String contentType){
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1) {
            return false;
        }
        String extension = filename.substring(dotIndex).toLowerCase();
        return ALLOWED_TYPES.containsKey(extension)
                && ALLOWED_TYPES.get(extension).contains(contentType);
    }

    public String getFileUrl(String filename){
        try {
            // Generate presigned URL valid for 7 days
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(filename)
                    .expiry(7 * 24 * 60 * 60) // 7 days
                    .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned URL for file: {}", filename, e);
            // Fallback to direct URL
            return url + "/" + bucketName + "/" + filename;
        }
    }

    public boolean isHealthy(){
        try{
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        }catch (Exception e){
            log.error("MinIO health check failed", e);
            return false;
        }
    }
}
