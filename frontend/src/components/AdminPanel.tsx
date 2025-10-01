import React, { useState } from 'react';
import {
  Box,
  Typography,
  Tabs,
  Tab,
  Paper,
  Alert,
} from '@mui/material';
import {
  People as PeopleIcon,
  Groups as GroupsIcon,
  BarChart as StatsIcon,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import UserManagement from './admin/UserManagement';
import GroupManagement from './admin/GroupManagement';
import AdminStats from './admin/AdminStats';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`admin-tabpanel-${index}`}
      aria-labelledby={`admin-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

const AdminPanel: React.FC = () => {
  const { user } = useAuth();
  const [tabValue, setTabValue] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  // Check if user is admin
  if (user?.role !== 'ROLE_ADMIN') {
    return (
      <Box sx={{ p: 3, textAlign: 'center' }}>
        <Alert severity="error">
          Access denied. You need administrator privileges to access this page.
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%', maxWidth: '1200px', mx: 'auto', p: 2 }}>
      <Paper sx={{ mb: 3, p: 3 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Admin Panel
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Welcome, {user?.username}. Manage users, groups, and monitor system statistics.
        </Typography>
      </Paper>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Paper sx={{ width: '100%' }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs
            value={tabValue}
            onChange={handleTabChange}
            aria-label="admin panel tabs"
            variant="fullWidth"
          >
            <Tab
              icon={<PeopleIcon />}
              label="Users"
              id="admin-tab-0"
              aria-controls="admin-tabpanel-0"
            />
            <Tab
              icon={<GroupsIcon />}
              label="Groups"
              id="admin-tab-1"
              aria-controls="admin-tabpanel-1"
            />
            <Tab
              icon={<StatsIcon />}
              label="Statistics"
              id="admin-tab-2"
              aria-controls="admin-tabpanel-2"
            />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={0}>
          <UserManagement onError={setError} />
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <GroupManagement onError={setError} />
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          <AdminStats onError={setError} />
        </TabPanel>
      </Paper>
    </Box>
  );
};

export default AdminPanel;
