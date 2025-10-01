# NotEx - Note exchange platform

My idea was to create a web app for students to exchange notes
by posting them on a platform for everybody else to access. 

The app allows users to **post their notes** and attach images and files and share them with other users. Notes can be summarized and translated using AI. Translations and summaries are cached, which prevents extensive use of tokens. Users can update and delete their notes. Users can also create, delete and update groups. Users can exchange messages with other users in groups. 

**I focused on the backend side of the project**. The frontend was implemented to showcase my backend project. Frontend was created with the help of AI and doesn't represent my frontend development skills.

If you wish to test the backend part of the project without using the frontend, you can use tools like Postman or curl to interact with the API endpoints. **Backend has its own docker-compose file** and [README](backend/README.md) file with instructions on how to run it.

I suggest you use the full stack setup to run the project with Docker. It makes it easier to test the backend.

---

## Features

- **JWT Authentication** - Secure login with token-based auth
- **Email verification** - Account verification via email code
- **Posting, updating and deleting notes** - Users can post, update and delete notes
- **Async file upload with MinIO integration** - Users can upload files to their notes
- **Searching notes** - Users can search notes by title or author
- **Creating, updating and deleting groups** - Users can create, update and delete groups
- **Sending messages** - Users can send messages to other users
- **Updating user profile** - Users can update their profile
- **AI summarization** - Comprehensive notes summarization with AI 
- **AI translation** - Multi-language notes translation using AI
- **Rate limiting** - Rate limiting for API endpoints
- **Caching** - Caching for frequently used data
- **Health monitoring** - Custom health indicators and metrics
- **Logging with SLF4J** - Structured logging for debugging and monitoring
- **Role-based access control** - Admin/User roles with appropriate permissions
---
## Tech stack

- **Spring Boot** - Backend framework
- **React** - Frontend framework
- **PostgreSQL** - Database
- **Docker** - Containerization
- **MinIO** - Object storage
- **OpenAI** - AI services
---
## Getting Started

### Prerequisites
- Docker and Docker Compose
- Git

### Full Stack Setup
1. Clone the repository
```bash
git clone https://github.com/notex-project/springboot-note-sharing.git
cd springboot-note-sharing
```

2. Create `.env` file in the root directory and add environment variables
```dotenv
JWT_SECRET=your-super-secret-jwt-key-change-in-production
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
OPENAI_API_KEY=your-openai-api-key
```

3. Run the full stack
```bash
docker-compose up -d
```

4. Access the application
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **MinIO Console**: http://localhost:9001 (admin/admin12345)

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

## Docker Setup

### Backend Only
If you want to run only the backend for API testing:

```bash
cd backend
docker-compose up -d
```

This will start:
- PostgreSQL database on port 5433
- MinIO object storage on ports 9000-9001
- Spring Boot backend on port 8080

### Services Overview
- **PostgreSQL**: Database on port 5433
- **MinIO**: Object storage on ports 9000-9001
- **Backend**: Spring Boot API on port 8080
- **Frontend**: React app on port 5173 (full stack only)

## API Documentation

### Authentication
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/verify` - Email verification
- `POST /auth/resend` - Resend verification code

### Users
- `GET /users/me` - Get current user profile
- `PATCH /users/me` - Update current user profile
- `GET /users/me/notes` - Get current user's notes
- `GET /users/me/groups` - Get current user's groups
- `GET /users/{username}` - Get user by username
- `GET /users/{username}/notes` - Get user's notes
- `GET /users/{username}/groups` - Get user's groups

### Notes
- `GET /notes` - Get all notes (with optional `partialName` search)
- `GET /notes/{noteId}` - Get note by ID
- `POST /notes` - Create note (multipart/form-data)
- `PATCH /notes/{noteId}` - Update note (multipart/form-data)
- `DELETE /notes/{noteId}` - Delete note
- `DELETE /notes/{noteId}/images/{imageId}` - Delete note image
- `GET /notes/{noteId}/summarize` - Summarize note with AI
- `GET /notes/{noteId}/translate` - Translate note with AI

### Groups
- `GET /groups` - Get groups (with optional `name` or `owner` search)
- `GET /groups/{groupId}` - Get group by ID
- `GET /groups/{groupId}/members` - Get group members
- `POST /groups` - Create group
- `PATCH /groups/{groupId}` - Update group
- `DELETE /groups/{groupId}` - Delete group
- `POST /groups/{groupId}/members` - Join group
- `POST /groups/{groupId}/members/{username}` - Add user to group
- `DELETE /groups/{groupId}/members/{username}` - Remove user from group
- `DELETE /groups/{groupId}/members/me` - Leave group

### Messages
- `GET /groups/{groupId}/messages` - Get group messages (paginated)
- `POST /groups/{groupId}/messages` - Send message to group

### Admin (Admin role required)
- `GET /users` - Get all users
- `PATCH /users/{username}` - Update user by admin
- `GET /users/{username}/notes/admin` - Get user's notes (admin view)

### Health Check
- `GET /health` - Basic health check
- `GET /health/rate-limiting` - Rate limiting health check

## Development

### Backend Development
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm install
npm run dev
```

## Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### API Testing
Use tools like Postman or curl to test the API endpoints. The backend runs on `http://localhost:8080` when started.



## Author
Jacek Kozakowski - [LinkedIn](https://www.linkedin.com/in/jacek-kozakowski/)

