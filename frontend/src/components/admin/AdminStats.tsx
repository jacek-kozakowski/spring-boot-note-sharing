import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Card,
  CardContent,
  CircularProgress,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
} from '@mui/material';
import {
  People as PeopleIcon,
  Groups as GroupsIcon,
  Description as NotesIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';
import { notexAPI } from '../../services/api';

interface AdminStatsProps {
  onError: (error: string) => void;
}

interface StatsData {
  totalUsers: number;
  totalGroups: number;
  totalNotes: number;
  recentUsers: any[];
  recentGroups: any[];
  recentNotes: any[];
}

const AdminStats: React.FC<AdminStatsProps> = ({ onError }) => {
  const [stats, setStats] = useState<StatsData | null>(null);
  const [loading, setLoading] = useState(true);

  const loadStats = async () => {
    try {
      setLoading(true);
      
      // Load all data in parallel
      const [usersResponse, groupsResponse, notesResponse] = await Promise.all([
        notexAPI.users.getAllUsers(),
        notexAPI.groups.getGroupsByName(''),
        notexAPI.users.getMyNotes(), // This gets current user's notes, we'd need a proper admin endpoint
      ]);

      const users = usersResponse.data;
      const groups = groupsResponse.data;
      const notes = notesResponse.data;

      // Calculate stats
      const totalUsers = users.length;
      const totalGroups = groups.length;
      const totalNotes = notes.length;

      // Get recent data (last 5)
      const recentUsers = users
        .sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5);

      const recentGroups = groups
        .sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5);

      const recentNotes = notes
        .sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5);

      setStats({
        totalUsers,
        totalGroups,
        totalNotes,
        recentUsers,
        recentGroups,
        recentNotes,
      });
    } catch (error: any) {
      console.error('Failed to load stats:', error);
      onError('Failed to load statistics. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStats();
  }, []);


  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!stats) {
    return (
      <Alert severity="error">
        Failed to load statistics. Please try again.
      </Alert>
    );
  }

  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        System Statistics
      </Typography>

      {/* Overview Cards */}
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mb: 4 }}>
        <Card sx={{ flex: '1 1 200px', minWidth: 200 }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <PeopleIcon sx={{ fontSize: 40, color: 'primary.main', mr: 2 }} />
              <Box>
                <Typography variant="h4" component="div">
                  {stats.totalUsers}
                </Typography>
                <Typography color="text.secondary">
                  Total Users
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>

        <Card sx={{ flex: '1 1 200px', minWidth: 200 }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <GroupsIcon sx={{ fontSize: 40, color: 'secondary.main', mr: 2 }} />
              <Box>
                <Typography variant="h4" component="div">
                  {stats.totalGroups}
                </Typography>
                <Typography color="text.secondary">
                  Total Groups
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>

        <Card sx={{ flex: '1 1 200px', minWidth: 200 }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <NotesIcon sx={{ fontSize: 40, color: 'success.main', mr: 2 }} />
              <Box>
                <Typography variant="h4" component="div">
                  {stats.totalNotes}
                </Typography>
                <Typography color="text.secondary">
                  Total Notes
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>

        <Card sx={{ flex: '1 1 200px', minWidth: 200 }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <TrendingUpIcon sx={{ fontSize: 40, color: 'warning.main', mr: 2 }} />
              <Box>
                <Typography variant="h4" component="div">
                  {Math.round((stats.totalNotes / Math.max(stats.totalUsers, 1)) * 100) / 100}
                </Typography>
                <Typography color="text.secondary">
                  Notes per User
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Box>

      {/* Recent Activity Tables */}
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
        <Paper sx={{ p: 2, flex: '1 1 300px', minWidth: 300 }}>
          <Typography variant="h6" gutterBottom>
            Recent Users
          </Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                  <TableRow>
                    <TableCell>User</TableCell>
                    <TableCell>Role</TableCell>
                    <TableCell>Status</TableCell>
                  </TableRow>
              </TableHead>
              <TableBody>
                {stats.recentUsers.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>
                      <Box>
                        <Typography variant="body2" fontWeight="medium">
                          {user.firstName} {user.lastName}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          @{user.username}
                        </Typography>
                      </Box>
                    </TableCell>
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
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>

        <Paper sx={{ p: 2, flex: '1 1 300px', minWidth: 300 }}>
          <Typography variant="h6" gutterBottom>
            Recent Groups
          </Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                  <TableRow>
                    <TableCell>Group</TableCell>
                    <TableCell>Owner</TableCell>
                    <TableCell>Type</TableCell>
                  </TableRow>
              </TableHead>
              <TableBody>
                {stats.recentGroups.map((group) => (
                  <TableRow key={group.id}>
                    <TableCell>
                      <Box>
                        <Typography variant="body2" fontWeight="medium">
                          {group.name}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          ID: {group.id}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {group.ownerUsername}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={group.isPrivate ? 'Private' : 'Public'}
                        color={group.isPrivate ? 'warning' : 'success'}
                        size="small"
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>

        <Paper sx={{ p: 2, flex: '1 1 300px', minWidth: 300 }}>
          <Typography variant="h6" gutterBottom>
            Recent Notes
          </Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                  <TableRow>
                    <TableCell>Title</TableCell>
                    <TableCell>Author</TableCell>
                    <TableCell>Status</TableCell>
                  </TableRow>
              </TableHead>
              <TableBody>
                {stats.recentNotes.map((note) => (
                  <TableRow key={note.id}>
                    <TableCell>
                      <Typography variant="body2" fontWeight="medium" noWrap>
                        {note.title}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {note.authorUsername}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label="Active"
                        color="success"
                        size="small"
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      </Box>
    </Box>
  );
};

export default AdminStats;
