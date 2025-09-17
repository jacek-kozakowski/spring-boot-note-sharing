import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Box,
  Typography,
  IconButton,
  Alert,
  Chip,
} from '@mui/material';
import {
  PersonRemove as RemoveIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { notexAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import type { Group, GroupMember } from '../types/group';

interface GroupMembersDialogProps {
  open: boolean;
  group: Group | null;
  onClose: () => void;
  onMemberRemoved: () => void;
}

const GroupMembersDialog: React.FC<GroupMembersDialogProps> = ({
  open,
  group,
  onClose,
  onMemberRemoved,
}) => {
  const { user } = useAuth();
  const [members, setMembers] = useState<GroupMember[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open && group) {
      loadMembers();
    }
  }, [open, group]);

  const loadMembers = async () => {
    if (!group) return;
    
    try {
      setLoading(true);
      setError(null);
      const response = await notexAPI.groups.getGroupMembers(group.id);
      setMembers(response.data);
    } catch (err: any) {
      setError('Failed to load group members');
      console.error('Error loading members:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveMember = async (member: GroupMember) => {
    if (!group) return;

    try {
      setLoading(true);
      setError(null);
      
      await notexAPI.groups.removeUserFromGroup(group.id, member.username);
      
      setMembers(prev => prev.filter(m => m.id !== member.id));
      onMemberRemoved();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to remove member');
    } finally {
      setLoading(false);
    }
  };

  const isOwner = user?.username === group?.ownerUsername;
  const canRemoveMember = (member: GroupMember) => {
    return isOwner && member.role !== 'OWNER' && member.username !== user?.username;
  };


  if (!group) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        Group Members: "{group.name}"
        <Typography variant="body2" color="text.secondary">
          {members?.length || 0} members
        </Typography>
      </DialogTitle>
      
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {loading && members.length === 0 ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
            <Typography color="text.secondary">Loading members...</Typography>
          </Box>
        ) : members.length === 0 ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
            <Typography color="text.secondary">No members found</Typography>
          </Box>
        ) : (
          <List>
            {members.map((member) => (
              <ListItem
                key={member.id.toString()}
                secondaryAction={
                  canRemoveMember(member) && (
                    <IconButton
                      edge="end"
                      onClick={() => handleRemoveMember(member)}
                      disabled={loading}
                      color="error"
                      title="Remove from group"
                    >
                      <RemoveIcon />
                    </IconButton>
                  )
                }
                sx={{
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: 1,
                  mb: 1,
                  bgcolor: 'background.paper',
                }}
              >
                <ListItemAvatar>
                  <Avatar sx={{ bgcolor: 'primary.main' }}>
                    <PersonIcon />
                  </Avatar>
                </ListItemAvatar>
                <ListItemText
                  primary={
                    <Box component="span" sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                      <Typography variant="subtitle1" component="span" sx={{ fontWeight: 600 }}>
                        {member.firstName} {member.lastName}
                      </Typography>
                      <Chip
                        label={member.role === 'OWNER' ? 'Owner' : 'Member'}
                        color={member.role === 'OWNER' ? 'primary' : 'default'}
                        size="small"
                        variant={member.role === 'OWNER' ? 'filled' : 'outlined'}
                      />
                      {member.username === user?.username && (
                        <Chip label="You" color="secondary" size="small" variant="filled" />
                      )}
                      {!member.enabled && (
                        <Chip label="Inactive" color="error" size="small" variant="outlined" />
                      )}
                    </Box>
                  }
                  secondary={
                    <Box component="span" sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                      <Typography variant="body2" color="text.secondary" component="span">
                        @{member.username}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" component="span">
                        Status: {member.enabled ? 'Active' : 'Inactive'}
                      </Typography>
                    </Box>
                  }
                />
              </ListItem>
            ))}
          </List>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default GroupMembersDialog;
