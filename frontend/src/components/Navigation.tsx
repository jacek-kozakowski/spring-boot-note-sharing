import React, { useState, useEffect } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Menu,
  MenuItem,
  Box,
  Avatar,
  InputBase,
  alpha,
  FormControl,
  Select,
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material/Select';
import {
  Search as SearchIcon,
  Add as AddIcon,
  Logout,
  Dashboard as DashboardIcon,
  Groups as GroupsIcon,
  Person as PersonIcon,
  Title as TitleIcon,
  Group as GroupIcon,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navigation: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchType, setSearchType] = useState<'title' | 'user' | 'group' | 'groupUser'>('title');

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
    handleClose();
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      if (searchType === 'group') {
        navigate(`/groups?search=${encodeURIComponent(searchQuery.trim())}`);
      } else if (searchType === 'groupUser') {
        navigate(`/groups?user=${encodeURIComponent(searchQuery.trim())}`);
      } else {
        const searchParam = searchType === 'user' ? 'user' : 'search';
        navigate(`/dashboard?${searchParam}=${encodeURIComponent(searchQuery.trim())}`);
      }
    }
  };

  const handleSearchTypeChange = (event: SelectChangeEvent) => {
    setSearchType(event.target.value as 'title' | 'user' | 'group' | 'groupUser');
  };

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  // Automatically switch search type based on current page
  useEffect(() => {
    if (location.pathname === '/groups') {
      setSearchType('group');
    } else if (location.pathname === '/dashboard') {
      setSearchType('title');
    }
  }, [location.pathname]);

  // Get available search options based on current page
  const getSearchOptions = () => {
    if (location.pathname === '/groups') {
      return [
        { value: 'group', label: 'By Group Name', icon: <GroupIcon sx={{ mr: 1, fontSize: 16 }} /> },
        { value: 'groupUser', label: 'By Username', icon: <PersonIcon sx={{ mr: 1, fontSize: 16 }} /> }
      ];
    } else {
      return [
        { value: 'title', label: 'By Title', icon: <TitleIcon sx={{ mr: 1, fontSize: 16 }} /> },
        { value: 'user', label: 'By User', icon: <PersonIcon sx={{ mr: 1, fontSize: 16 }} /> }
      ];
    }
  };

  return (
    <AppBar position="static" elevation={0} sx={{ bgcolor: 'white', color: 'text.primary' }}>
      <Toolbar>
        <Box sx={{ display: 'flex', alignItems: 'center', flexGrow: 1 }}>
          <Typography
            variant="h6"
            component="div"
            sx={{
              mr: 4,
              fontWeight: 700,
              color: 'primary.main',
              cursor: 'pointer',
            }}
            onClick={() => navigate('/dashboard')}
          >
            📝 Notex
          </Typography>

          <Button
            color="inherit"
            startIcon={<DashboardIcon />}
            onClick={() => navigate('/dashboard')}
            sx={{
              color: isActive('/dashboard') ? 'primary.main' : 'text.primary',
              fontWeight: isActive('/dashboard') ? 600 : 400,
            }}
          >
            Dashboard
          </Button>

          <Button
            color="inherit"
            startIcon={<AddIcon />}
            onClick={() => navigate('/notes/create')}
            sx={{
              color: isActive('/notes/create') ? 'primary.main' : 'text.primary',
              fontWeight: isActive('/notes/create') ? 600 : 400,
            }}
          >
            New Note
          </Button>

          <Button
            color="inherit"
            startIcon={<GroupsIcon />}
            onClick={() => navigate('/groups')}
            sx={{
              color: isActive('/groups') ? 'primary.main' : 'text.primary',
              fontWeight: isActive('/groups') ? 600 : 400,
            }}
          >
            Groups
          </Button>
        </Box>

        <Box
          component="form"
          onSubmit={handleSearch}
          sx={{
            position: 'relative',
            borderRadius: 1,
            backgroundColor: alpha('#000', 0.05),
            '&:hover': {
              backgroundColor: alpha('#000', 0.08),
            },
            marginRight: 2,
            marginLeft: 0,
            width: '400px',
            display: 'flex',
            alignItems: 'center',
          }}
        >
          <FormControl size="small" sx={{ minWidth: 120, mr: 1 }}>
            <Select
              value={searchType}
              onChange={handleSearchTypeChange}
              displayEmpty
              sx={{
                '& .MuiSelect-select': {
                  padding: '8px 12px',
                  fontSize: '0.875rem',
                },
                '& .MuiOutlinedInput-notchedOutline': {
                  border: 'none',
                },
              }}
            >
              {getSearchOptions().map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    {option.icon}
                    {option.label}
                  </Box>
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          
          <Box
            sx={{
              padding: '2px 4px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flex: 1,
            }}
          >
            <IconButton type="submit" sx={{ p: '10px' }} aria-label="search">
              <SearchIcon />
            </IconButton>
            <InputBase
              sx={{ ml: 1, flex: 1 }}
              placeholder={
                searchType === 'user' ? 'Search by username...' : 
                searchType === 'group' ? 'Search by group name...' : 
                searchType === 'groupUser' ? 'Search by username...' :
                'Search by title...'
              }
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              inputProps={{ 'aria-label': 'search notes' }}
            />
          </Box>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Typography variant="body2" sx={{ mr: 2, color: 'text.secondary' }}>
            Welcome, {user?.username}
          </Typography>
          <IconButton
            size="large"
            aria-label="account of current user"
            aria-controls="menu-appbar"
            aria-haspopup="true"
            onClick={handleMenu}
            color="inherit"
          >
            <Avatar sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}>
              {user?.username?.charAt(0).toUpperCase()}
            </Avatar>
          </IconButton>
          <Menu
            id="menu-appbar"
            anchorEl={anchorEl}
            anchorOrigin={{
              vertical: 'bottom',
              horizontal: 'right',
            }}
            keepMounted
            transformOrigin={{
              vertical: 'top',
              horizontal: 'right',
            }}
            open={Boolean(anchorEl)}
            onClose={handleClose}
          >
            <MenuItem onClick={() => { navigate('/dashboard'); handleClose(); }}>
              <DashboardIcon sx={{ mr: 1 }} />
              Dashboard
            </MenuItem>
            <MenuItem onClick={() => { navigate('/notes/create'); handleClose(); }}>
              <AddIcon sx={{ mr: 1 }} />
              New Note
            </MenuItem>
            <MenuItem onClick={() => { navigate('/groups'); handleClose(); }}>
              <GroupsIcon sx={{ mr: 1 }} />
              Groups
            </MenuItem>
            <MenuItem onClick={handleLogout}>
              <Logout sx={{ mr: 1 }} />
              Logout
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Navigation;
