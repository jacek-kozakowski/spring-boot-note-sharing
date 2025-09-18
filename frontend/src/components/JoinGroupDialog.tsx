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
  groupId?: number;
  groupName?: string;
  isPrivate?: boolean;
  onSuccess: () => void;
}

const JoinGroupDialog: React.FC<JoinGroupDialogProps> = ({
  open,
  onClose,
  groupId: propGroupId,
  groupName,
  isPrivate = false,
  onSuccess,
}) => {
  const navigate = useNavigate();
  const [groupId, setGroupId] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Update groupId when prop changes
  React.useEffect(() => {
    if (propGroupId) {
      setGroupId(propGroupId.toString());
    }
  }, [propGroupId]);

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
    
    const groupIdNum = propGroupId || parseInt(groupId.trim());
    
    if (!groupIdNum || isNaN(groupIdNum)) {
      setError('Group ID is required');
      return;
    }

    // Validate password only if group is private
    if (isPrivate && !password.trim()) {
      setError('Password is required for this private group');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      const joinData: JoinGroupRequestDto = { 
        password: isPrivate ? password.trim() : undefined 
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
        <DialogTitle>
          Join Group{groupName ? `: ${groupName}` : ''}
        </DialogTitle>
        
        <DialogContent>
          <Box sx={{ pt: 1 }}>
            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            {!propGroupId && (
              <>
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
              </>
            )}

            {propGroupId && (
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {groupName ? `You are joining the group "${groupName}".` : 'You are joining this group.'} 
                {isPrivate ? ' This is a private group and requires a password.' : ' This is a public group, no password needed.'}
              </Typography>
            )}

            {isPrivate && (
              <TextField
                margin="dense"
                label="Group Password"
                type="password"
                fullWidth
                variant="outlined"
                value={password}
                onChange={handlePasswordChange}
                disabled={loading}
                placeholder="Enter group password..."
                required
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
              disabled={loading || (!propGroupId && !groupId.trim()) || (isPrivate && !password.trim())}
            >
              {loading ? 'Joining...' : 'Join Group'}
            </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default JoinGroupDialog;
