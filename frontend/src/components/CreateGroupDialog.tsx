import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Alert,
  FormControlLabel,
  Switch,
} from '@mui/material';
import { notexAPI } from '../services/api';
import type { CreateGroupDto } from '../types/group';

interface CreateGroupDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const CreateGroupDialog: React.FC<CreateGroupDialogProps> = ({
  open,
  onClose,
  onSuccess,
}) => {
  const [formData, setFormData] = useState<CreateGroupDto>({
    name: '',
    description: '',
    isPrivate: false,
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleChange = (field: keyof CreateGroupDto) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
    setError(null);
  };

  const handleSwitchChange = (field: keyof CreateGroupDto) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    console.log('Switch changed:', field, 'to:', event.target.checked);
    setFormData(prev => {
      const newData = {
        ...prev,
        [field]: event.target.checked,
      };
      console.log('New formData:', newData);
      return newData;
    });
    setError(null);
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    
    if (!formData.name.trim()) {
      setError('Group name is required');
      return;
    }

    if (!formData.description.trim()) {
      setError('Group description is required');
      return;
    }

    if (formData.isPrivate && (!formData.password || formData.password.length < 8)) {
      setError('Password must be at least 8 characters for private groups');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      console.log('Sending formData:', formData);
      await notexAPI.groups.createGroup(formData);
      
      setFormData({ name: '', description: '', isPrivate: false, password: '' });
      onSuccess();
      onClose();
    } catch (err: unknown) {
      const errorMessage = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to create group';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setFormData({ name: '', description: '', isPrivate: false, password: '' });
      setError(null);
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit}>
        <DialogTitle>Create New Group</DialogTitle>
        
        <DialogContent>
          <Box sx={{ pt: 1 }}>
            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            <TextField
              autoFocus
              margin="dense"
              label="Group Name"
              fullWidth
              variant="outlined"
              value={formData.name}
              onChange={handleChange('name')}
              disabled={loading}
              required
              sx={{ mb: 2 }}
            />

            <TextField
              margin="dense"
              label="Group Description"
              fullWidth
              multiline
              rows={3}
              variant="outlined"
              value={formData.description}
              onChange={handleChange('description')}
              disabled={loading}
              required
              sx={{ mb: 2 }}
            />

            <FormControlLabel
              control={
                <Switch
                  checked={formData.isPrivate}
                  onChange={handleSwitchChange('isPrivate')}
                  disabled={loading}
                />
              }
              label="Private Group"
              sx={{ mb: 2 }}
            />

            {formData.isPrivate && (
              <TextField
                margin="dense"
                label="Group Password"
                type="password"
                fullWidth
                variant="outlined"
                value={formData.password}
                onChange={handleChange('password')}
                disabled={loading}
                required
                helperText="Password must be at least 8 characters"
                placeholder="Enter group password..."
              />
            )}
          </Box>
        </DialogContent>

        <DialogActions>
          <Button onClick={handleClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={loading || !formData.name.trim() || !formData.description.trim() || (formData.isPrivate && (!formData.password || formData.password.length < 8))}
          >
            {loading ? 'Creating...' : 'Create Group'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default CreateGroupDialog;
