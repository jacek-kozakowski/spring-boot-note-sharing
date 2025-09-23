#!/bin/bash

# Wait for MinIO to be ready
echo "Waiting for MinIO to be ready..."
until nc -z minio 9000; do
  echo "MinIO is not ready yet, waiting..."
  sleep 2
done

echo "MinIO is ready, setting up bucket policy..."

# Wait a bit more for MinIO to fully initialize
sleep 5

# Configure MinIO alias and set bucket policy to public
docker exec minio mc alias set local http://localhost:9000 admin admin12345
docker exec minio mc anonymous set public local/notex-notes

echo "Bucket policy set successfully"

# Start the Spring Boot application
exec java -jar /app/target/student-notes-0.0.1-SNAPSHOT.jar
