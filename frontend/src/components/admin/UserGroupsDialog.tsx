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
  Avatar,
} from '@mui/material';
import {
  Close as CloseIcon,
  Visibility as ViewIcon,
  Delete as DeleteIcon,
  People as PeopleIcon,
} from '@mui/icons-material';
import { notexAPI } from '../../services/api';

interface UserGroup {
  id: number;
  name: string;
  description: string;
  ownerUsername: string;
  membersCount: number;
  createdAt: string;
  isPrivate: boolean;
  isMember: boolean;
}

interface UserGroupsDialogProps {
  open: boolean;
  onClose: () => void;
  username: string;
  onError: (error: string) => void;
}

const UserGroupsDialog: React.FC<UserGroupsDialogProps> = ({ 
  open, 
  onClose, 
  username, 
  onError 
}) => {
  const [groups, setGroups] = useState<UserGroup[]>([]);
  const [loading, setLoading] = useState(false);

  const loadUserGroups = async () => {
    try {
      setLoading(true);
      const response = await notexAPI.users.getUserGroups(username);
      setGroups(response.data);
    } catch (error: any) {
      console.error('Failed to load user groups:', error);
      onError('Failed to load user groups. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open && username) {
      loadUserGroups();
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

  const handleViewGroup = (groupId: number) => {
    window.open(`/groups/${groupId}/messages`, '_blank');
  };

  const handleDeleteGroup = async (groupId: number) => {
    if (window.confirm('Are you sure you want to delete this group? This action cannot be undone.')) {
      try {
        await notexAPI.groups.deleteGroup(groupId);
        await loadUserGroups();
      } catch (error: any) {
        console.error('Failed to delete group:', error);
        onError('Failed to delete group. Please try again.');
      }
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">
            Groups by {username}
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
              {groups.length} groups found
            </Typography>
            
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Group</TableCell>
                    <TableCell>Owner</TableCell>
                    <TableCell>Members</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {groups.map((group) => (
                    <TableRow key={group.id} hover>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <Avatar sx={{ mr: 2, width: 32, height: 32 }}>
                            {group.name.charAt(0).toUpperCase()}
                          </Avatar>
                          <Box>
                            <Typography variant="body2" fontWeight="medium">
                              {group.name}
                            </Typography>
                            <Typography 
                              variant="caption" 
                              color="text.secondary" 
                              sx={{
                                display: '-webkit-box',
                                WebkitLineClamp: 1,
                                WebkitBoxOrient: 'vertical',
                                overflow: 'hidden',
                                maxWidth: 200,
                              }}
                            >
                              {group.description || 'No description'}
                            </Typography>
                          </Box>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {group.ownerUsername}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <PeopleIcon sx={{ mr: 0.5, fontSize: 16 }} />
                          {group.membersCount}
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={group.isPrivate ? 'Private' : 'Public'}
                          color={group.isPrivate ? 'warning' : 'success'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="caption">
                          {formatDate(group.createdAt)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Tooltip title="View Group">
                          <IconButton
                            size="small"
                            onClick={() => handleViewGroup(group.id)}
                            color="primary"
                          >
                            <ViewIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete Group">
                          <IconButton
                            size="small"
                            onClick={() => handleDeleteGroup(group.id)}
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
            
            {groups.length === 0 && (
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <Typography variant="body1" color="text.secondary">
                  No groups found for this user.
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

export default UserGroupsDialog;
