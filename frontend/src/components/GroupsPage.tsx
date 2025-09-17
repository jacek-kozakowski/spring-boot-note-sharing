import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Box,
  Paper,
  Button,
  TextField,
  InputAdornment,
  Grid,
  Fab,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
  IconButton,
  Menu,
  MenuItem,
} from '@mui/material';
import {
  Search as SearchIcon,
  Add as AddIcon,
  GroupAdd as JoinIcon,
  People as PeopleIcon,
  MoreVert as MoreIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Message as MessageIcon,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { notexAPI } from '../services/api';
import { useSearchParams } from 'react-router-dom';
import GroupCard from './GroupCard';
import CreateGroupDialog from './CreateGroupDialog';
import JoinGroupDialog from './JoinGroupDialog';
import EditGroupDialog from './EditGroupDialog';
import GroupMembersDialog from './GroupMembersDialog';
import type { Group } from '../types/group';

const GroupsPage: React.FC = () => {
  const { user, loading: authLoading } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  
  if (authLoading) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box display="flex" justifyContent="center" py={4}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }
  
  if (!user) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">
          You must be logged in to view groups.
        </Alert>
      </Container>
    );
  }
  
  const [groups, setGroups] = useState<Group[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState(searchParams.get('search') || '');
  const [userQuery, setUserQuery] = useState(searchParams.get('user') || '');
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [joinDialogOpen, setJoinDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [membersDialogOpen, setMembersDialogOpen] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState<Group | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [groupToDelete, setGroupToDelete] = useState<Group | null>(null);
  const [menuAnchor, setMenuAnchor] = useState<null | HTMLElement>(null);
  const [menuGroup, setMenuGroup] = useState<Group | null>(null);

  useEffect(() => {
    loadGroups();
  }, []);

  useEffect(() => {
    const searchParam = searchParams.get('search');
    const userParam = searchParams.get('user');
    
    if (searchParam) {
      setSearchQuery(searchParam);
      setUserQuery('');
      searchGroups(searchParam);
    } else if (userParam) {
      setUserQuery(userParam);
      setSearchQuery('');
      searchGroupsByOwner(userParam);
    } else {
      setSearchQuery('');
      setUserQuery('');
      loadGroups();
    }
  }, [searchParams]);

  const loadGroups = async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('=== LOADING GROUPS ===');
      console.log('Loading groups for user:', user?.username);
      console.log('Current timestamp:', new Date().toISOString());
      
      const response = await notexAPI.groups.getMyGroups();
      console.log('Groups API response:', response);
      console.log('Groups data:', response.data);
      console.log('Groups with detailed status:', response.data?.map((g: any) => ({
        id: g.id,
        name: g.name,
        isMember: g.isMember,
        member: g.member,
        ownerUsername: g.ownerUsername,
        isOwner: g.ownerUsername === user?.username,
        computed_isMember: g.isMember || g.member || (g.ownerUsername === user?.username)
      })));
      
      // Map backend response to frontend format
      const mappedGroups = (response.data || []).map((g: any) => ({
        ...g,
        isMember: g.isMember !== undefined ? g.isMember : g.member || false
      }));
      
      console.log('Mapped groups with isMember:', mappedGroups.map((g: any) => ({
        id: g.id,
        name: g.name,
        isMember: g.isMember,
        ownerUsername: g.ownerUsername
      })));
      
      setGroups(mappedGroups);
      console.log('=== GROUPS LOADED ===');
    } catch (err: any) {
      console.error('Error loading groups:', err);
      setError(`Failed to load groups: ${err.response?.data?.message || err.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      setSearchParams({ search: searchQuery.trim() });
      searchGroups(searchQuery.trim());
    } else if (userQuery.trim()) {
      setSearchParams({ user: userQuery.trim() });
      searchGroupsByOwner(userQuery.trim());
    } else {
      setSearchParams({});
      loadGroups();
    }
  };

  const searchGroups = async (query: string) => {
    try {
      setLoading(true);
      setError(null);
      const response = await notexAPI.groups.getGroupsByName(query);
      setGroups(response.data);
    } catch (err: any) {
      setError('Failed to search groups');
      console.error('Error searching groups:', err);
    } finally {
      setLoading(false);
    }
  };

  const searchGroupsByOwner = async (owner: string) => {
    try {
      setLoading(true);
      setError(null);
      const response = await notexAPI.groups.getGroupsByOwner(owner);
      setGroups(response.data);
    } catch (err: any) {
      setError('Failed to search groups by owner');
      console.error('Error searching groups by owner:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSuccess = () => {
    loadGroups();
  };

  const handleJoinSuccess = () => {
    console.log('Join successful, reloading groups...');
    console.log('Current groups before reload:', groups);
    // Clear any search params to show all groups
    setSearchParams({});
    // Force reload groups to get updated membership status
    setTimeout(() => {
      console.log('Reloading groups after timeout...');
      loadGroups();
    }, 100);
  };

  const handleMenuClick = (event: React.MouseEvent<HTMLElement>, group: Group) => {
    setMenuAnchor(event.currentTarget);
    setMenuGroup(group);
  };

  const handleMenuClose = () => {
    setMenuAnchor(null);
    setMenuGroup(null);
  };

  const handleEditGroup = (group: Group) => {
    setSelectedGroup(group);
    setEditDialogOpen(true);
    handleMenuClose();
  };

  const handleDeleteGroup = (group: Group) => {
    setGroupToDelete(group);
    setDeleteDialogOpen(true);
    handleMenuClose();
  };

  const handleDeleteConfirm = async () => {
    if (!groupToDelete) return;

    try {
      await notexAPI.groups.deleteGroup(groupToDelete.id);
      setGroups(groups.filter(group => group.id !== groupToDelete.id));
      setDeleteDialogOpen(false);
      setGroupToDelete(null);
    } catch (err: any) {
      setError('Failed to delete group');
      console.error('Error deleting group:', err);
    }
  };

  const handleLeaveGroup = async (group: Group) => {
    try {
      await notexAPI.groups.leaveGroup(group.id);
      setGroups(groups.filter(g => g.id !== group.id));
    } catch (err: any) {
      setError('Failed to leave group');
      console.error('Error leaving group:', err);
    }
  };

  const handleJoinGroup = (group: Group) => {
    setSelectedGroup(group);
    setJoinDialogOpen(true);
  };

  const handleViewMembers = (group: Group) => {
    setSelectedGroup(group);
    setMembersDialogOpen(true);
    handleMenuClose();
  };

  const isOwner = (group: Group | null) => {
    return group?.ownerUsername === user?.username;
  };

  const isMember = (group: Group | null) => {
    const isMemberResult = group?.isMember || isOwner(group);
    console.log(`isMember check for group ${group?.id} (${group?.name}): isMember=${group?.isMember}, isOwner=${isOwner(group)}, result=${isMemberResult}`);
    return isMemberResult;
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ fontWeight: 600 }}>
          {searchQuery ? `Search results for "${searchQuery}"` : 
           userQuery ? `Groups owned by "${userQuery}"` : 
           'My Groups'}
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
          <Paper component="form" onSubmit={handleSearch} sx={{ p: 2, flexGrow: 1, minWidth: 300 }}>
            <TextField
              fullWidth
              variant="outlined"
              placeholder="Search groups..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />
          </Paper>
          
          <Button
            variant="outlined"
            startIcon={<JoinIcon />}
            onClick={() => setJoinDialogOpen(true)}
            sx={{ minWidth: 150 }}
          >
            Join
          </Button>
        </Box>

        {searchQuery && (
          <Box sx={{ mb: 2 }}>
            <Button
              variant="outlined"
              onClick={() => {
                setSearchQuery('');
                setSearchParams({});
                loadGroups();
              }}
            >
              Clear search
            </Button>
          </Box>
        )}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box display="flex" justifyContent="center" py={4}>
          <CircularProgress />
        </Box>
      ) : groups.length === 0 ? (
        <Box textAlign="center" py={8}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            {searchQuery ? `No groups found matching "${searchQuery}"` : 
             userQuery ? `No groups found owned by "${userQuery}"` :
             'You are not a member of any groups yet'}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {searchQuery ? 'Try a different search term' : 
             userQuery ? 'Try a different username or check if this user has any groups' :
             'Create a group or join an existing one'}
          </Typography>
          {!searchQuery && !userQuery && (
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => setCreateDialogOpen(true)}
                size="large"
              >
                Create Group
              </Button>
              <Button
                variant="outlined"
                startIcon={<JoinIcon />}
                onClick={() => setJoinDialogOpen(true)}
                size="large"
              >
                Join Group
              </Button>
            </Box>
          )}
        </Box>
      ) : (
        <Grid container spacing={3}>
          {groups.map((group) => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={group.id.toString()}>
              <Box sx={{ position: 'relative' }}>
                <GroupCard
                  group={group}
                  isOwner={isOwner(group)}
                  isMember={isMember(group)}
                  onEdit={handleEditGroup}
                  onDelete={handleDeleteGroup}
                  onLeave={() => handleLeaveGroup(group)}
                  onJoin={() => handleJoinGroup(group)}
                />
                
                <IconButton
                  sx={{
                    position: 'absolute',
                    top: 12,
                    right: 12,
                    bgcolor: 'background.paper',
                    boxShadow: 1,
                    zIndex: 1,
                    '&:hover': {
                      bgcolor: 'grey.100',
                      boxShadow: 2,
                    },
                  }}
                  onClick={(e) => handleMenuClick(e, group)}
                >
                  <MoreIcon />
                </IconButton>
              </Box>
            </Grid>
          ))}
        </Grid>
      )}

      <Fab
        color="primary"
        aria-label="add"
        sx={{
          position: 'fixed',
          bottom: 16,
          right: 16,
        }}
        onClick={() => setCreateDialogOpen(true)}
      >
        <AddIcon />
      </Fab>

      <Menu
        anchorEl={menuAnchor}
        open={Boolean(menuAnchor)}
        onClose={handleMenuClose}
      >
        {menuGroup && [
          <MenuItem key="members" onClick={() => handleViewMembers(menuGroup)}>
            <PeopleIcon sx={{ mr: 1 }} />
            Members
          </MenuItem>,
          ...(isOwner(menuGroup) ? [
            <MenuItem key="edit" onClick={() => handleEditGroup(menuGroup)}>
              <EditIcon sx={{ mr: 1 }} />
              Edit
            </MenuItem>,
            <MenuItem key="delete" onClick={() => handleDeleteGroup(menuGroup)}>
              <DeleteIcon sx={{ mr: 1 }} />
              Delete
            </MenuItem>
          ] : []),
          ...(!isMember(menuGroup) ? [
            <MenuItem key="join" onClick={() => handleJoinGroup(menuGroup)}>
              <MessageIcon sx={{ mr: 1 }} />
              Join Group
            </MenuItem>
          ] : [])
        ]}
      </Menu>

      <CreateGroupDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onSuccess={handleCreateSuccess}
      />

      <JoinGroupDialog
        open={joinDialogOpen}
        onClose={() => setJoinDialogOpen(false)}
        onSuccess={handleJoinSuccess}
      />

      <EditGroupDialog
        open={editDialogOpen}
        group={selectedGroup}
        onClose={() => setEditDialogOpen(false)}
        onSuccess={handleCreateSuccess}
      />

      <GroupMembersDialog
        open={membersDialogOpen}
        group={selectedGroup}
        onClose={() => setMembersDialogOpen(false)}
        onMemberRemoved={loadGroups}
      />

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Group</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete the group "{groupToDelete?.name}"? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDeleteConfirm} color="error" variant="contained">
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default GroupsPage;
