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
  Typography,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { notexAPI } from '../services/api';
import type { JoinGroupRequestDto } from '../types/group';

interface JoinGroupDialogProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const JoinGroupDialog: React.FC<JoinGroupDialogProps> = ({
  open,
  onClose,
  onSuccess,
}) => {
  const navigate = useNavigate();
  const [groupId, setGroupId] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleGroupIdChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setGroupId(event.target.value);
    setError(null);
  };

  const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setPassword(event.target.value);
    setError(null);
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    
    if (!groupId.trim()) {
      setError('Group ID is required');
      return;
    }

    const groupIdNum = parseInt(groupId.trim());
    if (isNaN(groupIdNum)) {
      setError('Group ID must be a valid number');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      const joinData: JoinGroupRequestDto = { 
        password: password.trim() || undefined 
      };
      
      await notexAPI.groups.joinGroup(groupIdNum, joinData);
      
      console.log('Successfully joined group:', groupIdNum);
      setGroupId('');
      setPassword('');
      onSuccess();
      onClose();
      // Navigate to groups page to see updated list
      navigate('/groups');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to join group');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setGroupId('');
      setPassword('');
      setError(null);
      onClose();
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit}>
        <DialogTitle>Join Group</DialogTitle>
        
        <DialogContent>
          <Box sx={{ pt: 1 }}>
            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Enter the Group ID and password (if it's a private group) to join.
            </Typography>

            <TextField
              autoFocus
              margin="dense"
              label="Group ID"
              fullWidth
              variant="outlined"
              value={groupId}
              onChange={handleGroupIdChange}
              disabled={loading}
              required
              placeholder="Enter group ID..."
              type="number"
              sx={{ mb: 2 }}
            />

            <TextField
              margin="dense"
              label="Group Password (if private)"
              type="password"
              fullWidth
              variant="outlined"
              value={password}
              onChange={handlePasswordChange}
              disabled={loading}
              placeholder="Enter group password (optional for public groups)..."
            />
          </Box>
        </DialogContent>

        <DialogActions>
          <Button onClick={handleClose} disabled={loading}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={loading || !groupId.trim()}
          >
            {loading ? 'Joining...' : 'Join Group'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default JoinGroupDialog;
