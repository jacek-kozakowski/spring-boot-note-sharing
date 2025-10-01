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

# Install MinIO client if not present
if ! command -v mc &> /dev/null; then
  echo "Installing MinIO client..."
  wget -O mc https://dl.min.io/client/mc/release/linux-amd64/mc
  chmod +x mc
  mv mc /usr/local/bin/
fi

# Configure MinIO alias and set bucket policy to public
echo "Setting up MinIO alias..."
mc alias set local http://minio:9000 admin admin12345

echo "Creating bucket if it doesn't exist..."
mc mb local/notex-notes --ignore-existing

echo "Setting bucket policy to public read..."
mc anonymous set public local/notex-notes

echo "Bucket policy set successfully"

# Start the Spring Boot application
exec java -jar /app/target/student-notes-0.0.1-SNAPSHOT.jar
