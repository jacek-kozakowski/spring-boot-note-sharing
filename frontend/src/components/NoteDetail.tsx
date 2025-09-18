import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Container,
  Typography,
  Paper,
  Button,
  IconButton,
  Alert,
  CircularProgress,
  Grid,
  Card,
  CardMedia,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Person as PersonIcon,
  AccessTime as TimeIcon,
  Image as ImageIcon,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { notexAPI } from '../services/api';
import type { Note } from '../types/note';

const NoteDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [note, setNote] = useState<Note | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  useEffect(() => {
    if (id) {
      loadNote();
    }
  }, [id]);

  const loadNote = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      setError(null);
      const response = await notexAPI.notes.getNoteById(id);
      setNote(response.data);
    } catch (err: unknown) {
      setError('Failed to load note');
      console.error('Error loading note:', err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  const handleDelete = async () => {
    if (!note) return;

    try {
      await notexAPI.notes.deleteNote(note.id);
      navigate('/dashboard');
    } catch (err: unknown) {
      setError('Failed to delete note');
      console.error('Error deleting note:', err);
    }
  };

  const handleDeleteImage = async (imageId: number) => {
    if (!note || !id) return;

    try {
      await notexAPI.notes.deleteNoteImage(id, imageId);
      // Reload note to get updated images
      await loadNote();
    } catch (err: unknown) {
      setError('Failed to delete image');
      console.error('Error deleting image:', err);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pl-PL', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const isOwner = note?.ownerUsername === user?.username;

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Box display="flex" justifyContent="center" py={8}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error || !note) {
    return (
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Alert severity="error" sx={{ mb: 3 }}>
          {error || 'Notatka nie została znaleziona'}
        </Alert>
        <Button
          variant="outlined"
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/dashboard')}
        >
          Powrót do Dashboard
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton onClick={() => navigate('/dashboard')} sx={{ mr: 1 }}>
              <ArrowBackIcon />
            </IconButton>
            <Typography variant="h4" component="h1" sx={{ fontWeight: 600 }}>
              {note.title}
            </Typography>
          </Box>

          {isOwner && (
            <Box>
              <IconButton
                onClick={() => navigate(`/notes/${note.id}/edit`)}
                color="primary"
                sx={{ mr: 1 }}
              >
                <EditIcon />
              </IconButton>
              <IconButton
                onClick={() => setDeleteDialogOpen(true)}
                color="error"
              >
                <DeleteIcon />
              </IconButton>
            </Box>
          )}
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <PersonIcon sx={{ fontSize: 20, mr: 0.5, color: 'text.secondary' }} />
            <Typography variant="body2" color="text.secondary">
              {note.ownerUsername}
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <TimeIcon sx={{ fontSize: 20, mr: 0.5, color: 'text.secondary' }} />
            <Typography variant="body2" color="text.secondary">
              {formatDate(note.createdAt)}
            </Typography>
          </Box>
          {note.updatedAt !== note.createdAt && (
            <Typography variant="body2" color="text.secondary">
              (Updated: {formatDate(note.updatedAt)})
            </Typography>
          )}
        </Box>
      </Box>

      <Paper sx={{ p: 4, mb: 3 }}>
        <Typography
          variant="body1"
          sx={{
            whiteSpace: 'pre-wrap',
            lineHeight: 1.6,
            fontSize: '1.1rem',
          }}
        >
          {note.content}
        </Typography>
      </Paper>

      {note.images && note.images.length > 0 && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
            <ImageIcon sx={{ mr: 1, color: 'primary.main' }} />
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              Obrazy ({note.images.length})
            </Typography>
          </Box>
          
          <Grid container spacing={2}>
            {note.images.map((image, index) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={image.id}>
                <Card>
                  <CardMedia
                    component="img"
                    height="200"
                    image={image.url}
                    alt={`Image ${index + 1}`}
                    sx={{ cursor: 'pointer' }}
                    onClick={() => window.open(image.url, '_blank')}
                  />
                  {isOwner && (
                    <Box sx={{ p: 1, display: 'flex', justifyContent: 'center' }}>
                      <Button
                        size="small"
                        color="error"
                        startIcon={<DeleteIcon />}
                        onClick={() => handleDeleteImage(image.id)}
                      >
                        Delete
                      </Button>
                    </Box>
                  )}
                </Card>
              </Grid>
            ))}
          </Grid>
        </Paper>
      )}

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Note</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete "{note.title}"? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default NoteDetail;
