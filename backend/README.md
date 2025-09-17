# Notex Backend 

**Spring Boot based backend** for note sharing application **Notex**.

## Features
- **JWT Authentication** - Secure login with token-based auth
- **Email Verification** - Account verification via email code
- **Role-Based Access** - Admin/User roles with appropriate permissions
- **Logging with SLF4J** - Structured logging for debugging and monitoring

## Technologies
- **Java 21** - Programming Language
- **Spring Boot** - Java Framework
- **Spring Security** - Authentication and Authorization
- **Spring Data JPA** - Object-Relational Mapping
- **PostgreSQL** - Database
- **Maven** - Dependency Management
- **JUnit** - Unit Testing
- **Mockito** - Mocking Framework
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
```

4. Build and compose docker containers
```bash
docker-compose up --build -d
```

5. Run backend

MacOS:
```bash
./mvnw spring-boot:run
```

Windows:
```bash
mvnw.cmd spring-boot:run
```

## Tests
To run tests use:

MacOS:
```bash
./mvnw test
```

Windows:
```bash
mvnw.cmd test
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
    "username":"testusername",
    "password": "password123",
    "email": "test@example.com",
    "firstName":"Test",
    "lastName":"User"
}
```

Response 201
```json
{
    "id": 2,
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
    "username":"testusername",
    "verificationCode":"<Verification Code>"
}
```

### Login

```http
POST /auth/login
```

Request
```json
{
    "username":"testusername",
    "password":"password123"
}
```

Response 200
```json
{
    "token": "<JWT Token>",
    "tokenExpirationTime": 3600000
}
```