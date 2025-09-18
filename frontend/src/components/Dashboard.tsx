import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Container,
  Typography,
  Card,
  CardContent,
  CardActions,
  Button,
  Grid,
  TextField,
  InputAdornment,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Alert,
  Paper,
  Divider,
} from '@mui/material';
import {
  Search as SearchIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  Image as ImageIcon,
  Person as PersonIcon,
  AccessTime as TimeIcon,
} from '@mui/icons-material';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { notexAPI } from '../services/api';
import SmartFab from './SmartFab';
import type { Note } from '../types/note';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAuth();
  
  const [notes, setNotes] = useState<Note[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState(searchParams.get('search') || '');
  const [userQuery, setUserQuery] = useState(searchParams.get('user') || '');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [noteToDelete, setNoteToDelete] = useState<Note | null>(null);

  const loadMyNotes = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('Loading my notes...');
      const response = await notexAPI.users.getMyNotes();
      console.log('My notes response:', response.data);
      setNotes(response.data);
    } catch (err: unknown) {
      setError('Failed to load notes');
      console.error('Error loading notes:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const searchNotes = useCallback(async (query: string) => {
    if (!query.trim()) {
      loadMyNotes();
      return;
    }

    try {
      setLoading(true);
      setError(null);
      console.log('Searching for notes with query:', query.trim());
      const response = await notexAPI.notes.getNotesByPartialName(query.trim());
      console.log('Search response:', response.data);
      setNotes(response.data);
    } catch (err: unknown) {
      setError('Failed to search notes');
      console.error('Error searching notes:', err);
    } finally {
      setLoading(false);
    }
  }, [loadMyNotes]);

  const searchNotesByUser = useCallback(async (username: string) => {
    if (!username.trim()) {
      loadMyNotes();
      return;
    }

    try {
      setLoading(true);
      setError(null);
      console.log('Searching for notes by user:', username.trim());
      const response = await notexAPI.users.getUserNotes(username.trim());
      console.log('User notes response:', response.data);
      setNotes(response.data);
    } catch (err: unknown) {
      setError(`Failed to load notes for user ${username}`);
      console.error('Error loading user notes:', err);
    } finally {
      setLoading(false);
    }
  }, [loadMyNotes]);

  const loadNotes = useCallback(async () => {
    if (searchQuery) {
      await searchNotes(searchQuery);
    } else if (userQuery) {
      await searchNotesByUser(userQuery);
    } else {
      await loadMyNotes();
    }
  }, [searchQuery, userQuery, searchNotes, searchNotesByUser, loadMyNotes]);

  useEffect(() => {
    loadNotes();
  }, [loadNotes]);

  useEffect(() => {
    const searchParam = searchParams.get('search');
    const userParam = searchParams.get('user');
    
    if (searchParam) {
      setSearchQuery(searchParam);
      setUserQuery('');
      searchNotes(searchParam);
    } else if (userParam) {
      setUserQuery(userParam);
      setSearchQuery('');
      searchNotesByUser(userParam);
    } else {
      setSearchQuery('');
      setUserQuery('');
      loadMyNotes();
    }
  }, [searchParams, searchNotes, searchNotesByUser, loadMyNotes]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      setSearchParams({ search: searchQuery.trim() });
      searchNotes(searchQuery.trim());
    } else if (userQuery.trim()) {
      setSearchParams({ user: userQuery.trim() });
      searchNotesByUser(userQuery.trim());
    } else {
      setSearchParams({});
      loadMyNotes();
    }
  };

  const handleDeleteClick = (note: Note) => {
    setNoteToDelete(note);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!noteToDelete) return;

    try {
      await notexAPI.notes.deleteNote(noteToDelete.id);
      setNotes(notes.filter(note => note.id !== noteToDelete.id));
      setDeleteDialogOpen(false);
      setNoteToDelete(null);
    } catch (err: unknown) {
      setError('Failed to delete note');
      console.error('Error deleting note:', err);
    }
  };

  const handleDeleteCancel = () => {
    setDeleteDialogOpen(false);
    setNoteToDelete(null);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const truncateContent = (content: string, maxLength: number = 150) => {
    if (content.length <= maxLength) return content;
    return content.substring(0, maxLength) + '...';
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 600 }}>
          {searchQuery ? `Search results for "${searchQuery}"` : 
           userQuery ? `Notes by user "${userQuery}"` : 
           'My Notes'}
        </Typography>
        
        <Paper component="form" onSubmit={handleSearch} sx={{ p: 2, mb: 3 }}>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="Search notes by title..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
        </Paper>

        {(searchQuery || userQuery) && (
          <Box sx={{ mb: 2 }}>
            <Button
              variant="outlined"
              onClick={() => {
                setSearchQuery('');
                setUserQuery('');
                setSearchParams({});
              }}
            >
              Clear search
            </Button>
          </Box>
        )}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box display="flex" justifyContent="center" py={4}>
          <CircularProgress />
        </Box>
      ) : notes.length === 0 ? (
        <Box textAlign="center" py={8}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            {searchQuery ? `No notes found matching "${searchQuery}"` : 
             userQuery ? `No notes found for user "${userQuery}"` : 
             'No notes found'}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {searchQuery ? 'Try a different search term or check if other users have created notes with this title' : 
             userQuery ? 'This user may not have any notes or the username might be incorrect' :
             'Create your first note to get started'}
          </Typography>
          {!searchQuery && !userQuery && (
            <Button
              variant="contained"
              onClick={() => navigate('/notes/create')}
              size="large"
            >
              Create Note
            </Button>
          )}
        </Box>
      ) : (
        <Grid container spacing={3}>
          {notes.map((note) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={note.id}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                  '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                  },
                }}
              >
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" component="h2" gutterBottom noWrap>
                    {note.title}
                  </Typography>
                  
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{
                      mb: 2,
                      display: '-webkit-box',
                      WebkitLineClamp: 3,
                      WebkitBoxOrient: 'vertical',
                      overflow: 'hidden',
                    }}
                  >
                    {truncateContent(note.content)}
                  </Typography>

                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <PersonIcon sx={{ fontSize: 16, mr: 0.5, color: 'text.secondary' }} />
                    <Typography variant="caption" color="text.secondary">
                      {note.ownerUsername}
                    </Typography>
                  </Box>

                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <TimeIcon sx={{ fontSize: 16, mr: 0.5, color: 'text.secondary' }} />
                    <Typography variant="caption" color="text.secondary">
                      {formatDate(note.createdAt)}
                    </Typography>
                  </Box>

                  {note.images && note.images.length > 0 && (
                    <Chip
                      icon={<ImageIcon />}
                      label={`${note.images.length} image${note.images.length > 1 ? 's' : ''}`}
                      size="small"
                      color="primary"
                      variant="outlined"
                    />
                  )}
                </CardContent>

                <Divider />

                <CardActions sx={{ justifyContent: 'space-between', p: 2 }}>
                  <Button
                    size="small"
                    startIcon={<ViewIcon />}
                    onClick={() => navigate(`/notes/${note.id}`)}
                  >
                    View
                  </Button>
                  
                  {note.ownerUsername === user?.username && (
                    <Box>
                      <IconButton
                        size="small"
                        onClick={() => navigate(`/notes/${note.id}/edit`)}
                        color="primary"
                      >
                        <EditIcon />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => handleDeleteClick(note)}
                        color="error"
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Box>
                  )}
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      <SmartFab />

      <Dialog open={deleteDialogOpen} onClose={handleDeleteCancel}>
        <DialogTitle>Delete Note</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete "{noteToDelete?.title}"? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDeleteCancel}>Cancel</Button>
          <Button onClick={handleDeleteConfirm} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Dashboard;
