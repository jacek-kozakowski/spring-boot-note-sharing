import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  CircularProgress,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Close as CloseIcon,
  Visibility as ViewIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
} from '@mui/icons-material';
import { notexAPI } from '../../services/api';

interface UserNote {
  id: number;
  title: string;
  content: string;
  authorUsername: string;
  createdAt: string;
  updatedAt: string;
  isDeleted: boolean;
}

interface UserNotesDialogProps {
  open: boolean;
  onClose: () => void;
  username: string;
  onError: (error: string) => void;
}

const UserNotesDialog: React.FC<UserNotesDialogProps> = ({ 
  open, 
  onClose, 
  username, 
  onError 
}) => {
  const [notes, setNotes] = useState<UserNote[]>([]);
  const [loading, setLoading] = useState(false);

  const loadUserNotes = async () => {
    try {
      setLoading(true);
      const response = await notexAPI.users.getUserNotes(username);
      setNotes(response.data);
    } catch (error: any) {
      console.error('Failed to load user notes:', error);
      onError('Failed to load user notes. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open && username) {
      loadUserNotes();
    }
  }, [open, username]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pl-PL', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleViewNote = (noteId: number) => {
    window.open(`/notes/${noteId}`, '_blank');
  };

  const handleEditNote = (noteId: number) => {
    window.open(`/notes/${noteId}/edit`, '_blank');
  };

  const handleDeleteNote = async (noteId: number) => {
    if (window.confirm('Are you sure you want to delete this note? This action cannot be undone.')) {
      try {
        await notexAPI.notes.deleteNote(noteId);
        await loadUserNotes();
      } catch (error: any) {
        console.error('Failed to delete note:', error);
        onError('Failed to delete note. Please try again.');
      }
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">
            Notes by {username}
          </Typography>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : (
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {notes.length} notes found
            </Typography>
            
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Title</TableCell>
                    <TableCell>Content Preview</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell>Updated</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {notes.map((note) => (
                    <TableRow key={note.id} hover>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">
                          {note.title}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography 
                          variant="body2" 
                          color="text.secondary"
                          sx={{
                            display: '-webkit-box',
                            WebkitLineClamp: 2,
                            WebkitBoxOrient: 'vertical',
                            overflow: 'hidden',
                            maxWidth: 200,
                          }}
                        >
                          {note.content}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={note.isDeleted ? 'Deleted' : 'Active'}
                          color={note.isDeleted ? 'error' : 'success'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="caption">
                          {formatDate(note.createdAt)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="caption">
                          {formatDate(note.updatedAt)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Tooltip title="View Note">
                          <IconButton
                            size="small"
                            onClick={() => handleViewNote(note.id)}
                            color="primary"
                          >
                            <ViewIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Edit Note">
                          <IconButton
                            size="small"
                            onClick={() => handleEditNote(note.id)}
                            color="primary"
                          >
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete Note">
                          <IconButton
                            size="small"
                            onClick={() => handleDeleteNote(note.id)}
                            color="error"
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            
            {notes.length === 0 && (
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <Typography variant="body1" color="text.secondary">
                  No notes found for this user.
                </Typography>
              </Box>
            )}
          </Box>
        )}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
};

export default UserNotesDialog;
