package com.notex.student_notes.config.health;

import com.notex.student_notes.minio.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MinioHealthIndicator implements HealthIndicator {
    private final MinioService minioService;

    @Override
    public Health health() {
        try{
            boolean isHealthy = minioService.isHealthy();
            if(isHealthy) {
                return Health.up()
                        .withDetail("service", "MinIO")
                        .withDetail("status", "Available")
                        .withDetail("bucket", minioService.getBucketName())
                        .build();
            }else {
                return Health.down()
                        .withDetail("service", "MinIO")
                        .withDetail("status", "Unavailable")
                        .withDetail("error", "Cannot connect to MinIO")
                        .build();
            }
        }catch (Exception e){
            return Health.down()
                    .withDetail("service", "MinIO")
                    .withDetail("status", "Error")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

}
