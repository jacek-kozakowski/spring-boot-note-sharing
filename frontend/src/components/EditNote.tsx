import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  TextField,
  Button,
  Paper,
  Grid,
  IconButton,
  Alert,
  CircularProgress,
  Card,
  CardMedia,
  CardActions,
  Chip,
  Divider,
  Checkbox,
  FormControlLabel,
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Add as AddIcon,
  Delete as DeleteIcon,
  Image as ImageIcon,
  Save as SaveIcon,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { notexAPI } from '../services/api';
import type { Note, UpdateNoteDto } from '../types/note';

const EditNote: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [note, setNote] = useState<Note | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState<UpdateNoteDto>({
    title: '',
    content: '',
    newImages: [],
    removeImageIds: [],
  });
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [imagesToRemove, setImagesToRemove] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (id) {
      loadNote();
    }
  }, [id]);

  const loadNote = async () => {
    if (!id) return;

    try {
      setLoading(true);
      setError(null);
      const response = await notexAPI.notes.getNoteById(id);
      const noteData = response.data;
      
      // Check if user is the owner
      if (noteData.ownerUsername !== user?.username) {
        setError('You are not authorized to edit this note');
        return;
      }

      setNote(noteData);
      setFormData({
        title: noteData.title,
        content: noteData.content,
        newImages: [],
        removeImageIds: [],
      });
    } catch (err: any) {
      setError('Failed to load note');
      console.error('Error loading note:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: keyof UpdateNoteDto) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: e.target.value,
    }));
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    // Validate file types
    const validFiles = files.filter(file => 
      file.type.startsWith('image/') && file.size <= 10 * 1024 * 1024 // 10MB limit
    );

    if (validFiles.length !== files.length) {
      setError('Some files were skipped. Only image files under 10MB are allowed.');
      return;
    }

    setFormData(prev => ({
      ...prev,
      newImages: [...(prev.newImages || []), ...validFiles],
    }));

    // Create previews
    validFiles.forEach(file => {
      const reader = new FileReader();
      reader.onload = (e) => {
        setImagePreviews(prev => [...prev, e.target?.result as string]);
      };
      reader.readAsDataURL(file);
    });
  };

  const removeNewImage = (index: number) => {
    setFormData(prev => ({
      ...prev,
      newImages: prev.newImages?.filter((_, i) => i !== index) || [],
    }));
    setImagePreviews(prev => prev.filter((_, i) => i !== index));
  };

  const toggleImageRemoval = (imageId: number) => {
    const newImagesToRemove = new Set(imagesToRemove);
    if (newImagesToRemove.has(imageId)) {
      newImagesToRemove.delete(imageId);
    } else {
      newImagesToRemove.add(imageId);
    }
    setImagesToRemove(newImagesToRemove);
    
    setFormData(prev => ({
      ...prev,
      removeImageIds: Array.from(newImagesToRemove),
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.title?.trim() || !formData.content?.trim()) {
      setError('Title and content are required');
      return;
    }

    if (!id) return;

    try {
      setSaving(true);
      setError(null);

      const formDataToSend = new FormData();
      formDataToSend.append('title', formData.title);
      formDataToSend.append('content', formData.content);
      
      if (formData.newImages) {
        formData.newImages.forEach((file) => {
          formDataToSend.append('newImages', file);
        });
      }

      if (formData.removeImageIds && formData.removeImageIds.length > 0) {
        formData.removeImageIds.forEach((id) => {
          formDataToSend.append('removeImageIds', id.toString());
        });
      }

      await notexAPI.notes.updateNote(id, formDataToSend);
      navigate(`/notes/${id}`);
    } catch (err: unknown) {
      const errorMessage = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to update note';
      setError(errorMessage);
      console.error('Error updating note:', err);
    } finally {
      setSaving(false);
    }
  };

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
          {error || 'Note not found'}
        </Alert>
        <Button
          variant="outlined"
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/dashboard')}
        >
          Back to Dashboard
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <IconButton onClick={() => navigate(`/notes/${id}`)} sx={{ mr: 1 }}>
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 600 }}>
            Edit Note
          </Typography>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ p: 4 }}>
        <form onSubmit={handleSubmit}>
          <Grid container spacing={3}>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Title"
                value={formData.title}
                onChange={handleInputChange('title')}
                required
                placeholder="Enter note title..."
                variant="outlined"
              />
            </Grid>

            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Content"
                value={formData.content}
                onChange={handleInputChange('content')}
                required
                multiline
                rows={12}
                placeholder="Write your note content here..."
                variant="outlined"
              />
            </Grid>

            {note.images && note.images.length > 0 && (
              <Grid size={{ xs: 12 }}>
                <Divider sx={{ my: 2 }}>
                  <Chip icon={<ImageIcon />} label="Current Images" />
                </Divider>
                
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Select images to remove:
                </Typography>
                
                <Grid container spacing={2}>
                  {note.images.map((image, index) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={image.id}>
                      <Card>
                        <CardMedia
                          component="img"
                          height="140"
                          image={image.url}
                          alt={`Image ${index + 1}`}
                        />
                        <CardActions>
                          <FormControlLabel
                            control={
                              <Checkbox
                                checked={imagesToRemove.has(image.id)}
                                onChange={() => toggleImageRemoval(image.id)}
                                color="error"
                              />
                            }
                            label="Remove"
                            sx={{ m: 0 }}
                          />
                        </CardActions>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </Grid>
            )}

            <Grid size={{ xs: 12 }}>
              <Divider sx={{ my: 2 }}>
                <Chip icon={<ImageIcon />} label="Add New Images" />
              </Divider>
              
              <Box sx={{ mb: 2 }}>
                <Button
                  variant="outlined"
                  component="label"
                  startIcon={<AddIcon />}
                  disabled={saving}
                >
                  Add Images
                  <input
                    type="file"
                    hidden
                    multiple
                    accept="image/*"
                    onChange={handleImageChange}
                  />
                </Button>
                <Typography variant="caption" display="block" sx={{ mt: 1, color: 'text.secondary' }}>
                  You can select multiple images. Maximum 10MB per file.
                </Typography>
              </Box>

              {imagePreviews.length > 0 && (
                <Grid container spacing={2}>
                  {imagePreviews.map((preview, index) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={index}>
                      <Card>
                        <CardMedia
                          component="img"
                          height="140"
                          image={preview}
                          alt={`Preview ${index + 1}`}
                        />
                        <CardActions>
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => removeNewImage(index)}
                            disabled={saving}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </CardActions>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              )}
            </Grid>

            <Grid size={{ xs: 12 }}>
              <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                <Button
                  variant="outlined"
                  onClick={() => navigate(`/notes/${id}`)}
                  disabled={saving}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  startIcon={saving ? <CircularProgress size={20} /> : <SaveIcon />}
                  disabled={saving || !formData.title?.trim() || !formData.content?.trim()}
                >
                  {saving ? 'Saving...' : 'Save Changes'}
                </Button>
              </Box>
            </Grid>
          </Grid>
        </form>
      </Paper>
    </Container>
  );
};

export default EditNote;
