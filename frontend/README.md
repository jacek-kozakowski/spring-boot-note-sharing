# Notex Frontend

A comprehensive React TypeScript frontend for the Notex student notes application.

## Features

### 🔐 Authentication
- User registration with email verification
- Secure login/logout functionality
- Protected routes and authorization

### 📝 Note Management
- **Create Notes**: Rich text editor with image upload support
- **View Notes**: Detailed note view with image gallery
- **Edit Notes**: Full editing capabilities with image management
- **Delete Notes**: Safe deletion with confirmation dialogs
- **Search Notes**: Search through all notes by title

### 🖼️ Image Management
- Upload multiple images per note
- Image preview and management
- Delete individual images
- Support for common image formats (PNG, JPG, etc.)
- 10MB file size limit per image

### 🎨 Modern UI/UX
- Material-UI components for consistent design
- Responsive layout for all screen sizes
- Dark/light theme support
- Intuitive navigation and user experience
- Loading states and error handling

### 🔍 Search & Discovery
- Search all notes by title
- Filter by user notes vs. all notes
- Real-time search results
- Clear search functionality

## Technology Stack

- **React 19** with TypeScript
- **Material-UI (MUI)** for components
- **React Router** for navigation
- **Axios** for API communication
- **Vite** for build tooling

## Getting Started

### Prerequisites
- Node.js 18+ 
- npm or yarn
- Backend API running on http://localhost:8080

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start development server:
```bash
npm run dev
```

3. Open http://localhost:5173 in your browser

### Building for Production

```bash
npm run build
```

## Project Structure

```
src/
├── components/          # React components
│   ├── Dashboard.tsx    # Main dashboard with notes list
│   ├── Login.tsx        # Login form
│   ├── Register.tsx     # Registration form
│   ├── Navigation.tsx   # Top navigation bar
│   ├── CreateNote.tsx   # Note creation form
│   ├── EditNote.tsx     # Note editing form
│   └── NoteDetail.tsx   # Note detail view
├── context/             # React context providers
│   └── AuthContext.tsx  # Authentication context
├── services/            # API services
│   └── api.ts          # API client configuration
├── types/               # TypeScript type definitions
│   ├── user.ts         # User-related types
│   └── note.ts         # Note-related types
├── App.tsx             # Main app component
└── main.tsx           # App entry point
```

## Key Features Explained

### Dashboard
- Displays user's notes in a responsive grid
- Search functionality to find notes by title
- Quick actions: view, edit, delete
- Floating action button for creating new notes

### Note Creation/Editing
- Rich text input with validation
- Multiple image upload with preview
- Form validation and error handling
- Responsive design for all devices

### Image Management
- Drag & drop or click to upload
- Image preview before upload
- Individual image deletion
- Support for common image formats

### Search
- Real-time search as you type
- Search across all notes (not just user's)
- Clear search functionality
- URL-based search state

## API Integration

The frontend integrates with the backend API through:
- RESTful endpoints for CRUD operations
- JWT token authentication
- Multipart form data for file uploads
- Error handling and user feedback

## Responsive Design

- Mobile-first approach
- Breakpoints for tablet and desktop
- Touch-friendly interface
- Optimized for all screen sizes

## Error Handling

- Comprehensive error messages
- Loading states for better UX
- Form validation
- Network error handling
- User-friendly error dialogs

## Security

- JWT token management
- Protected routes
- Input validation
- Secure file upload handling

## Development

### Code Style
- TypeScript for type safety
- ESLint for code quality
- Consistent component structure
- Proper error handling

### Performance
- Lazy loading where appropriate
- Optimized re-renders
- Efficient state management
- Image optimization

## Contributing

1. Follow TypeScript best practices
2. Use Material-UI components consistently
3. Write responsive components
4. Include proper error handling
5. Add loading states for async operations

## License

This project is part of the Notex student notes application.