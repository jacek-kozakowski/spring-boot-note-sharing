import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Chip,
  Button,
  TextField,
  InputAdornment,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Avatar,
  Tooltip,
} from '@mui/material';
import {
  Search as SearchIcon,
  Visibility as ViewIcon,
  Refresh as RefreshIcon,
  Delete as DeleteIcon,
  People as PeopleIcon,
} from '@mui/icons-material';
import { notexAPI } from '../../services/api';

interface AdminGroup {
  id: number;
  name: string;
  description: string;
  ownerUsername: string;
  membersCount: number;
  createdAt: string;
  isPrivate: boolean;
}

interface GroupManagementProps {
  onError: (error: string) => void;
}

const GroupManagement: React.FC<GroupManagementProps> = ({ onError }) => {
  const [groups, setGroups] = useState<AdminGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedGroup, setSelectedGroup] = useState<AdminGroup | null>(null);
  const [viewDialogOpen, setViewDialogOpen] = useState(false);
  const [membersDialogOpen, setMembersDialogOpen] = useState(false);
  const [groupMembers, setGroupMembers] = useState<any[]>([]);

  const loadGroups = async () => {
    try {
      setLoading(true);
      // Since there's no direct admin endpoint for all groups, we'll use the regular endpoint
      // In a real app, you'd want a dedicated admin endpoint
      const response = await notexAPI.groups.getGroupsByName('');
      setGroups(response.data);
    } catch (error: any) {
      console.error('Failed to load groups:', error);
      onError('Failed to load groups. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const loadGroupMembers = async (groupId: number) => {
    try {
      const response = await notexAPI.groups.getGroupMembers(groupId);
      setGroupMembers(response.data);
    } catch (error: any) {
      console.error('Failed to load group members:', error);
      onError('Failed to load group members. Please try again.');
    }
  };

  useEffect(() => {
    loadGroups();
  }, []);

  const handleViewGroup = (group: AdminGroup) => {
    setSelectedGroup(group);
    setViewDialogOpen(true);
  };

  const handleViewMembers = async (group: AdminGroup) => {
    setSelectedGroup(group);
    await loadGroupMembers(group.id);
    setMembersDialogOpen(true);
  };

  const handleDeleteGroup = async (groupId: number) => {
    if (window.confirm('Are you sure you want to delete this group? This action cannot be undone.')) {
      try {
        await notexAPI.groups.deleteGroup(groupId);
        await loadGroups();
      } catch (error: any) {
        console.error('Failed to delete group:', error);
        onError('Failed to delete group. Please try again.');
      }
    }
  };

  const filteredGroups = groups.filter(group =>
    group.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    group.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
    group.ownerUsername.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pl-PL', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" component="h2">
          Group Management ({groups.length} groups)
        </Typography>
        <Button
          startIcon={<RefreshIcon />}
          onClick={loadGroups}
          variant="outlined"
        >
          Refresh
        </Button>
      </Box>

      <TextField
        fullWidth
        placeholder="Search groups by name, description, or owner..."
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        sx={{ mb: 3 }}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon />
            </InputAdornment>
          ),
        }}
      />

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
            {filteredGroups.map((group) => (
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
                      <Typography variant="caption" color="text.secondary" noWrap sx={{ maxWidth: 200 }}>
                        {group.description || 'No description'}
                      </Typography>
                    </Box>
                  </Box>
                </TableCell>
                <TableCell>{group.ownerUsername}</TableCell>
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
                <TableCell>{formatDate(group.createdAt)}</TableCell>
                <TableCell>
                  <Tooltip title="View Details">
                    <IconButton
                      size="small"
                      onClick={() => handleViewGroup(group)}
                      color="primary"
                    >
                      <ViewIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="View Members">
                    <IconButton
                      size="small"
                      onClick={() => handleViewMembers(group)}
                      color="primary"
                    >
                      <PeopleIcon />
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

      {/* View Group Dialog */}
      <Dialog open={viewDialogOpen} onClose={() => setViewDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Group Details</DialogTitle>
        <DialogContent>
          {selectedGroup && (
            <Box sx={{ mt: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <Avatar sx={{ mr: 2, width: 64, height: 64 }}>
                  {selectedGroup.name.charAt(0).toUpperCase()}
                </Avatar>
                <Box>
                  <Typography variant="h6">{selectedGroup.name}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    ID: {selectedGroup.id}
                  </Typography>
                </Box>
              </Box>
              
              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mb: 2 }}>
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">Owner</Typography>
                  <Typography variant="body1">{selectedGroup.ownerUsername}</Typography>
                </Box>
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">Members</Typography>
                  <Typography variant="body1">{selectedGroup.membersCount}</Typography>
                </Box>
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">Type</Typography>
                  <Chip
                    label={selectedGroup.isPrivate ? 'Private' : 'Public'}
                    color={selectedGroup.isPrivate ? 'warning' : 'success'}
                    size="small"
                  />
                </Box>
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">Created</Typography>
                  <Typography variant="body1">{formatDate(selectedGroup.createdAt)}</Typography>
                </Box>
              </Box>
              
              <Box>
                <Typography variant="subtitle2" color="text.secondary">Description</Typography>
                <Typography variant="body1">
                  {selectedGroup.description || 'No description provided'}
                </Typography>
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setViewDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Group Members Dialog */}
      <Dialog open={membersDialogOpen} onClose={() => setMembersDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Group Members - {selectedGroup?.name}
        </DialogTitle>
        <DialogContent>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Member</TableCell>
                  <TableCell>Role</TableCell>
                  <TableCell>Joined</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {groupMembers.map((member, index) => (
                  <TableRow key={index}>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <Avatar sx={{ mr: 2, width: 32, height: 32 }}>
                          {member.username?.charAt(0).toUpperCase() || 'U'}
                        </Avatar>
                        <Box>
                          <Typography variant="body2" fontWeight="medium">
                            {member.firstName} {member.lastName}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            @{member.username}
                          </Typography>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={member.isOwner ? 'Owner' : 'Member'}
                        color={member.isOwner ? 'error' : 'default'}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      {member.joinedAt ? formatDate(member.joinedAt) : 'Unknown'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setMembersDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default GroupManagement;
