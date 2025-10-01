import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline, Box } from '@mui/material';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Navigation from './components/Navigation';
import NoteDetail from './components/NoteDetail';
import CreateNote from './components/CreateNote';
import EditNote from './components/EditNote';
import GroupsPage from './components/GroupsPage';
import GroupMessagesPage from './components/GroupMessagesPage';
import AdminPanel from './components/AdminPanel';
import ErrorBoundary from './components/ErrorBoundary';

// Create theme
const theme = createTheme({
  palette: {
    primary: {
      main: '#667eea',
      light: '#8da4f7',
      dark: '#4a56d7',
    },
    secondary: {
      main: '#764ba2',
      light: '#9b6bc7',
      dark: '#5c3a85',
    },
    background: {
      default: '#f5f5f5',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 600,
    },
    h5: {
      fontWeight: 600,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          fontWeight: 500,
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12,
        },
      },
    },
  },
});

// Protected Route Component
const ProtectedRoute = ({ children, adminOnly = false }: { children: React.ReactNode, adminOnly?: boolean }) => {
  const { user, isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  if (adminOnly && user?.role !== 'ROLE_ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }
  
  return children;
};

// Main App Content
const AppContent = () => {
  const { isAuthenticated } = useAuth();

  return (
    <Box 
      sx={{ 
        minHeight: '100vh', 
        width: '100vw',
        margin: 0,
        padding: 0,
        background: isAuthenticated 
          ? 'background.default' 
          : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: isAuthenticated ? 'flex-start' : 'center'
      }}
    >
      {/* Header for non-authenticated users */}
      {!isAuthenticated && (
        <Box sx={{ textAlign: 'center', mt: 4, mb: 2, color: 'white' }}>
          <h1 style={{ fontSize: '3rem', marginBottom: '0.5rem', fontWeight: 700, textShadow: '2px 2px 4px rgba(0, 0, 0, 0.3)' }}>
            📝 Notex
          </h1>
          <p style={{ fontSize: '1.2rem', opacity: 0.9, margin: 0 }}>
            Manage and share your study notes with ease
          </p>
        </Box>
      )}
      
      {isAuthenticated && <Navigation />}
      
      <Box 
        component="main" 
        sx={{ 
          pt: isAuthenticated ? 2 : 0,
          width: '100%',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: isAuthenticated ? 'auto' : 'auto',
          flex: isAuthenticated ? 'none' : 1
        }}
      >
        <Routes>
          <Route 
            path="/login" 
            element={isAuthenticated ? <Navigate to="/dashboard" /> : <Login />} 
          />
          <Route 
            path="/register" 
            element={isAuthenticated ? <Navigate to="/dashboard" /> : <Register />} 
          />
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/notes/create" 
            element={
              <ProtectedRoute>
                <CreateNote />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/notes/:id" 
            element={
              <ProtectedRoute>
                <NoteDetail />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/notes/:id/edit" 
            element={
              <ProtectedRoute>
                <EditNote />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/groups" 
            element={
              <ProtectedRoute>
                <GroupsPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/groups/:groupId/messages" 
            element={
              <ProtectedRoute>
                <GroupMessagesPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/admin" 
            element={
              <ProtectedRoute adminOnly>
                <AdminPanel />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/" 
            element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} />} 
          />
        </Routes>
      </Box>
    </Box>
  );
};

// Main App Component
const App = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <ErrorBoundary>
        <AuthProvider>
          <Router>
            <AppContent />
          </Router>
        </AuthProvider>
      </ErrorBoundary>
    </ThemeProvider>
  );
};

export default App;
