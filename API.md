# API Documentation

## Authentication
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/verify` - Email verification
- `POST /auth/resend` - Resend verification code

## Users
- `GET /users/me` - Get current user profile
- `PATCH /users/me` - Update current user profile
- `GET /users/me/notes` - Get current user's notes
- `GET /users/me/groups` - Get current user's groups
- `GET /users/{username}` - Get user by username
- `GET /users/{username}/notes` - Get user's notes
- `GET /users/{username}/groups` - Get user's groups

## Notes
- `GET /notes` - Get all notes (with optional `partialName` search)
- `GET /notes/{noteId}` - Get note by ID
- `POST /notes` - Create note (multipart/form-data)
- `PATCH /notes/{noteId}` - Update note (multipart/form-data)
- `DELETE /notes/{noteId}` - Delete note
- `DELETE /notes/{noteId}/images/{imageId}` - Delete note image
- `GET /notes/{noteId}/summarize` - Summarize note with AI
- `GET /notes/{noteId}/translate` - Translate note with AI

## Groups
- `GET /groups` - Get groups (with optional `name` or `owner` search)
- `GET /groups/{groupId}` - Get group by ID
- `GET /groups/{groupId}/members` - Get group members
- `POST /groups` - Create a group
- `PATCH /groups/{groupId}` - Update group
- `DELETE /groups/{groupId}` - Delete group
- `POST /groups/{groupId}/members` - Join group
- `POST /groups/{groupId}/members/{username}` - Add user to group
- `DELETE /groups/{groupId}/members/{username}` - Remove user from group
- `DELETE /groups/{groupId}/members/me` - Leave group

## Messages
- `GET /groups/{groupId}/messages` - Get group messages (paginated)
- `POST /groups/{groupId}/messages` - Send a message to group

## Admin (Admin role required)
- `GET /users` - Get all users
- `PATCH /users/{username}` - Update user by admin
- `GET /users/{username}/notes/admin` - Get user's notes (admin view)

## Health Check
- `GET /health` - Basic health check
- `GET /health/rate-limiting` - Rate limiting health check