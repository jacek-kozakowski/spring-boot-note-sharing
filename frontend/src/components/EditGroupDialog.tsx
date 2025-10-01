import React, { useState, useEffect } from 'react';
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
import type { Group, UpdateGroupDto } from '../types/group';

interface EditGroupDialogProps {
  open: boolean;
  group: Group | null;
  onClose: () => void;
  onSuccess: () => void;
}

const EditGroupDialog: React.FC<EditGroupDialogProps> = ({
  open,
  group,
  onClose,
  onSuccess,
}) => {
  const [formData, setFormData] = useState<UpdateGroupDto>({
    name: '',
    description: '',
    isPrivate: false,
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (group) {
      setFormData({
        name: group.name || '',
        description: group.description || '',
        isPrivate: group.isPrivate || false,
        password: '',
      });
    }
  }, [group]);

  const handleChange = (field: keyof UpdateGroupDto) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value,
    }));
    setError(null);
  };

  const handleSwitchChange = (field: keyof UpdateGroupDto) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.checked,
    }));
    setError(null);
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    
    if (!group) return;

    if (!formData.name?.trim()) {
      setError('Group name is required');
      return;
    }

    if (!formData.description?.trim()) {
      setError('Group description is required');
      return;
    }

    // Only validate password if group is being changed to private or password is being changed
    if (formData.isPrivate && !group.isPrivate && (!formData.password || formData.password.length < 8)) {
      setError('Password must be at least 8 characters when making group private');
      return;
    }
    
    if (formData.password && formData.password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      const updateData: UpdateGroupDto = {};

      // Only include changed fields
      if (formData.name !== group.name) {
        updateData.name = formData.name;
      }
      
      if (formData.description !== group.description) {
        updateData.description = formData.description;
      }
      
      if (formData.isPrivate !== group.isPrivate) {
        updateData.isPrivate = formData.isPrivate;
      }

      if (formData.password && formData.password.trim()) {
        updateData.password = formData.password;
      }

      // Check if there are any changes
      if (Object.keys(updateData).length === 0) {
        setError('No changes detected');
        return;
      }

      await notexAPI.groups.updateGroup(group.id, updateData);
      
      onSuccess();
      onClose();
    } catch (err: unknown) {
      const errorMessage = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to update group';
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
        <DialogTitle>Edit Group</DialogTitle>
        
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
                label={group.isPrivate ? "New Group Password (leave empty to keep current)" : "Group Password"}
                type="password"
                fullWidth
                variant="outlined"
                value={formData.password}
                onChange={handleChange('password')}
                disabled={loading}
                helperText={group.isPrivate ? "Enter new password or leave empty to keep current password" : "Enter password for private group"}
                placeholder={group.isPrivate ? "Enter new group password..." : "Enter group password..."}
                required={!group.isPrivate}
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
            disabled={loading || !formData.name?.trim() || !formData.description?.trim()}
          >
            {loading ? 'Updating...' : 'Update Group'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default EditGroupDialog;

