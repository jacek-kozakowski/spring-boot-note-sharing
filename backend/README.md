# Notex Backend 

**Spring Boot based backend** for note sharing application **Notex**.

This backend represents my **main focus and development effort** in the Notex project. It's where I concentrated most of my time and attention, building a comprehensive, production-ready foundation for the entire note-sharing ecosystem.

## Features
- **JWT Authentication** - Secure login with token-based auth
- **Email Verification** - Account verification via email code
- **Role-Based Access** - Admin/User roles with appropriate permissions
- **Logging with SLF4J** - Structured logging for debugging and monitoring
- **AI-Powered Features** - Note summarization and multi-language translation
- **Rate Limiting** - Rate limiting for endpoints
- **Caching** - Caching for frequently used data
- **File Upload System** - Synchronous Multi-format file support (images, PDFs, documents)
- **MinIO Integration** - S3-compatible object storage for files
- **Group Messaging** - Real-time group communication system
- **Health Monitoring** - Custom health indicators and metrics

## Technologies
- **Java 21** - Programming Language
- **Spring Boot** - Java Framework
- **Spring Security** - Authentication and Authorization
- **Spring Data JPA** - Object-Relational Mapping
- **PostgreSQL** - Database
- **Maven** - Dependency Management
- **JUnit and Mockito** - Testing
- **Docker** - Containerization

## Getting Started
1. Clone the repository

```bash
git clone https://github.com/notex-project/springboot-note-sharing.git
cd springboot-note-sharing/backend
```

2. Set Java 21 as default JDK

MacOS:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)  
```

3. Set environment variables in `.env` file or `application.properties` file
```properties
JWT_SECRET=your-super-secret-jwt-key-change-in-production
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
OPENAI_API_KEY=your-openai-api-key
```

4. Run the application

**Option A: With Docker (Recommended)**
```bash
cd backend
docker-compose up -d
```

**Option B: Run locally**
```bash
# MacOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

## Docker Configuration

### Backend Only Setup

This backend includes its own `compose.yaml` for running only the backend with its dependencies (PostgreSQL and MinIO).

#### Quick Start
```bash
cd backend
docker-compose up -d
```

#### Available Services
- **Backend**: http://localhost:8080
- **PostgreSQL**: localhost:5433
- **MinIO Console**: http://localhost:9001 (admin/admin12345)

#### Commands
```bash
# Start services
cd backend
docker-compose up -d

# Stop services
cd backend
docker-compose down

# Rebuild backend
cd backend
docker-compose build backend
docker-compose up -d

# View logs
cd backend
docker-compose logs backend
```

#### Differences from Main compose.yaml
- Uses different container names (with `-backend` suffix)
- Uses port 5433 for PostgreSQL (instead of 5432)
- Does not include frontend
- Uses the same volumes as the main compose

### Full Stack (Backend + Frontend)

To run the complete application with frontend, use the main `compose.yaml` in the project root:

```bash
# From project root
docker-compose up -d
```

## Tests
To run tests, use:

MacOS:
```bash
./mvnw test
```

Windows:
```bash
mvnw.cmd test
```


## Health Check
To check if the backend is running, use:
```bash
curl -s http://localhost:8080/actuator/health
```
  
# API Usage Examples

Base URL: `http://localhost:8080`

Authentication: add header `Authorization: Bearer <JWT Token>` for protected endpoints.

## Auth

### Register
```http
POST /auth/register
```

Request
```json
{
    "username": "testusername",
    "password": "password123",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User"
}
```

Response 201
```json
{
    "id": 1,
    "username": "testusername",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "role": "ROLE_USER",
    "enabled": false
}
```

### Verify
```http
POST /auth/verify
```
Request
```json
{
    "username": "testusername",
    "verificationCode": "<Verification Code>"
}
```

### Login

```http
POST /auth/login
```

Request
```json
{
    "username": "testusername",
    "password": "password123"
}
```

Response 200
```json
{
    "token": "<JWT Token>",
    "tokenExpirationTime": 3600000
}
```

### Resend Verification Code
```http 
POST /auth/resend
```
Request
```json
{
    "username": "testusername"
}
```
Response 200
```json
{
    "message": "Verification code was sent successfully. Check your email."
}
```

## Notes

### Get All Notes
```http
GET /notes?partialName=<search_term>
```

### Create Note
```http
POST /notes
Content-Type: multipart/form-data
```

Request (multipart/form-data)
```
title: "My Note Title"
content: "Note content here"
images: [file1, file2, ...]
```

### Update Note
```http
PATCH /notes/{noteId}
Content-Type: multipart/form-data
```

### Delete Note
```http
DELETE /notes/{noteId}
```

### Search Notes
```http
GET /notes/search?query=<search_term>&filter=<filter_type>
```

### Summarize Note
```http
GET /notes/{noteId}/summarize
```

### Translate Note
```http
GET /notes/{noteId}/translate?language=<language_code>
```

## Groups

### Get All Groups
```http
GET /groups?name=<group_name>&owner=<owner_username>
```

### Create Group
```http
POST /groups
```

Request
```json
{
    "name": "Study Group",
    "description": "Group for studying together",
    "isPrivate": true,
    "password": "optional_password"
}
```

### Join Group
```http
POST /groups/{groupId}/members
```

Request
```json
{
    "password": "group_password_if_private"
}
```

### Leave Group
```http
DELETE /groups/{groupId}/members/me
```

## Messages

### Get Group Messages
```http
GET /groups/{groupId}/messages?page=<page_number>&size=<page_size>
```

### Send Message
```http
POST /groups/{groupId}/messages
```

Request
```json
{
    "content": "Hello everyone!"
}
```

## Users

### Update Current User
```http
PATCH /users/me
```

Request
```json
{
    "firstName": "Updated Name",
    "lastName": "Updated Last Name",
    "email": "newemail@example.com"
}
```

## Admin Endpoints

### Get All Users (Admin Only)
```http
GET /users
```

### Update User (Admin Only)
```http
PATCH /users/{username}
```

## Health Check

### Basic Health Check
```http
GET /health
```

## Configuration

### Database Configuration

The application uses PostgreSQL with the following default settings:
- **Host**: localhost:5433
- **Database**: notex
- **Username**: notex_user
- **Password**: secret

### MinIO Configuration

File storage is handled by MinIO:
- **URL**: http://localhost:9000
- **Access Key**: admin
- **Secret Key**: admin12345
- **Bucket**: notex-notes

## Project Structure
```
src/
├── main/
│   ├── java/com/notex/student_notes/
│   │   ├── auth/           # Authentication & Authorization
│   │   ├── user/           # User management
│   │   ├── note/           # Note management
│   │   ├── group/          # Group management
│   │   ├── message/        # Messaging system
│   │   ├── ai/             # AI features (summarization, translation)
│   │   ├── config/         # Configuration classes
│   │   └── upload/         # File upload handling
│   └── resources/
│       └── application.properties
└── test/                   # Test files
```

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Ensure PostgreSQL is running on port 5433
   - Check database credentials in `application.properties`
   - If using PostgreSQL locally, change port to 5432 in `application.properties`

2. **Email Not Sending**
   - Verify email credentials in .env file
   - Check if 2-factor authentication is enabled for Gmail

3. **File Upload Issues**
   - Ensure MinIO is running on port 9000
   - Check MinIO credentials and bucket configuration

4. **JWT Token Issues**
   - Verify JWT_SECRET is set in environment variables
   - Check token expiration settings
