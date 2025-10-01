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
  Edit as EditIcon,
  Visibility as ViewIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { notexAPI } from '../../services/api';
import type { updateUserDto } from '../../types/user';
import UserNotesDialog from './UserNotesDialog';
import UserGroupsDialog from './UserGroupsDialog';

interface AdminViewUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  role: string;
  verificationCode: string | null;
  verificationExpiration: string | null;
}

interface UserManagementProps {
  onError: (error: string) => void;
}

const UserManagement: React.FC<UserManagementProps> = ({ onError }) => {
  const [users, setUsers] = useState<AdminViewUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedUser, setSelectedUser] = useState<AdminViewUser | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [viewDialogOpen, setViewDialogOpen] = useState(false);
  const [editFormData, setEditFormData] = useState<updateUserDto>({});
  const [notesDialogOpen, setNotesDialogOpen] = useState(false);
  const [groupsDialogOpen, setGroupsDialogOpen] = useState(false);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const response = await notexAPI.users.getAllUsers();
      setUsers(response.data);
    } catch (error: any) {
      console.error('Failed to load users:', error);
      onError('Failed to load users. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleEditUser = (user: AdminViewUser) => {
    setSelectedUser(user);
    setEditFormData({
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
    });
    setEditDialogOpen(true);
  };

  const handleViewUser = async (user: AdminViewUser) => {
    setSelectedUser(user);
    setViewDialogOpen(true);
  };

  const handleViewNotes = (user: AdminViewUser) => {
    setSelectedUser(user);
    setNotesDialogOpen(true);
  };

  const handleViewGroups = (user: AdminViewUser) => {
    setSelectedUser(user);
    setGroupsDialogOpen(true);
  };

  const handleSaveEdit = async () => {
    if (!selectedUser) return;

    try {
      await notexAPI.users.updateUserByAdmin(selectedUser.username, editFormData);
      await loadUsers();
      setEditDialogOpen(false);
      setSelectedUser(null);
    } catch (error: any) {
      console.error('Failed to update user:', error);
      onError('Failed to update user. Please try again.');
    }
  };

  const filteredUsers = users.filter(user =>
    user.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
    user.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
    user.firstName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    user.lastName.toLowerCase().includes(searchQuery.toLowerCase())
  );


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
          User Management ({users.length} users)
        </Typography>
        <Button
          startIcon={<RefreshIcon />}
          onClick={loadUsers}
          variant="outlined"
        >
          Refresh
        </Button>
      </Box>

      <TextField
        fullWidth
        placeholder="Search users by username, email, or name..."
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
                <TableCell>User</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Notes</TableCell>
                <TableCell>Groups</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
          </TableHead>
          <TableBody>
            {filteredUsers.map((user) => (
              <TableRow key={user.id} hover>
                <TableCell>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <Avatar sx={{ mr: 2, width: 32, height: 32 }}>
                      {user.username.charAt(0).toUpperCase()}
                    </Avatar>
                    <Box>
                      <Typography variant="body2" fontWeight="medium">
                        {user.firstName} {user.lastName}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        @{user.username}
                      </Typography>
                    </Box>
                  </Box>
                </TableCell>
                <TableCell>{user.email}</TableCell>
                <TableCell>
                  <Chip
                    label={user.role}
                    color={user.role === 'ADMIN' ? 'error' : 'default'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={user.enabled ? 'Active' : 'Inactive'}
                    color={user.enabled ? 'success' : 'warning'}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Button
                    size="small"
                    variant="outlined"
                    onClick={() => handleViewNotes(user)}
                  >
                    View Notes
                  </Button>
                </TableCell>
                <TableCell>
                  <Button
                    size="small"
                    variant="outlined"
                    onClick={() => handleViewGroups(user)}
                  >
                    View Groups
                  </Button>
                </TableCell>
                <TableCell>
                  <Tooltip title="View Details">
                    <IconButton
                      size="small"
                      onClick={() => handleViewUser(user)}
                      color="primary"
                    >
                      <ViewIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Edit User">
                    <IconButton
                      size="small"
                      onClick={() => handleEditUser(user)}
                      color="primary"
                    >
                      <EditIcon />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* View User Dialog */}
      <Dialog open={viewDialogOpen} onClose={() => setViewDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>User Details</DialogTitle>
        <DialogContent>
          {selectedUser && (
            <Box sx={{ mt: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <Avatar sx={{ mr: 2, width: 64, height: 64 }}>
                  {selectedUser.username.charAt(0).toUpperCase()}
                </Avatar>
                <Box>
                  <Typography variant="h6">
                    {selectedUser.firstName} {selectedUser.lastName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    @{selectedUser.username}
                  </Typography>
                </Box>
              </Box>
              
              <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mb: 2 }}>
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">Email</Typography>
                  <Typography variant="body1">{selectedUser.email}</Typography>
                </Box>
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">Role</Typography>
                  <Chip
                    label={selectedUser.role}
                    color={selectedUser.role === 'ADMIN' ? 'error' : 'default'}
                    size="small"
                  />
                </Box>
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">Status</Typography>
                  <Chip
                    label={selectedUser.enabled ? 'Active' : 'Inactive'}
                    color={selectedUser.enabled ? 'success' : 'warning'}
                    size="small"
                  />
                </Box>
                <Box>
                  <Typography variant="subtitle2" color="text.secondary">Verification</Typography>
                  <Typography variant="body1">
                    {selectedUser.verificationCode ? 'Pending' : 'Verified'}
                  </Typography>
                </Box>
              </Box>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setViewDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Edit User Dialog */}
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Edit User</DialogTitle>
        <DialogContent>
          {selectedUser && (
            <Box sx={{ mt: 2 }}>
              <TextField
                fullWidth
                label="First Name"
                value={editFormData.firstName || ''}
                onChange={(e) => setEditFormData(prev => ({ ...prev, firstName: e.target.value }))}
                margin="normal"
              />
              <TextField
                fullWidth
                label="Last Name"
                value={editFormData.lastName || ''}
                onChange={(e) => setEditFormData(prev => ({ ...prev, lastName: e.target.value }))}
                margin="normal"
              />
              <TextField
                fullWidth
                label="Email"
                type="email"
                value={editFormData.email || ''}
                onChange={(e) => setEditFormData(prev => ({ ...prev, email: e.target.value }))}
                margin="normal"
              />
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleSaveEdit} variant="contained">Save Changes</Button>
        </DialogActions>
      </Dialog>

      {/* User Notes Dialog */}
      <UserNotesDialog
        open={notesDialogOpen}
        onClose={() => setNotesDialogOpen(false)}
        username={selectedUser?.username || ''}
        onError={onError}
      />

      {/* User Groups Dialog */}
      <UserGroupsDialog
        open={groupsDialogOpen}
        onClose={() => setGroupsDialogOpen(false)}
        username={selectedUser?.username || ''}
        onError={onError}
      />
    </Box>
  );
};

export default UserManagement;
