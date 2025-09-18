import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Alert,
  InputAdornment,
  IconButton,
  CircularProgress
} from '@mui/material';
import { Visibility, VisibilityOff, Person, Lock, NoteAlt } from '@mui/icons-material';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState({ username: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prevState => ({ ...prevState, [name]: value }));
    if (error) setError('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.username || !formData.password) {
      setError('Proszę wypełnić wszystkie pola');
      return;
    }
    
    setLoading(true);
    setError('');
    setSuccess('');
    
    console.log('Attempting login with:', { username: formData.username, password: '***' });
    
    try {
      await login(formData.username, formData.password);
      setSuccess('Login successful!');
      navigate('/dashboard');
    } catch (err: unknown) {
      if ((err as any)?.response?.status === 401) {
        setError('Invalid username or password');
      }
      else if ((err as any)?.response?.status === 403) {
        setError('Please verify your email before logging in');
      }
      else if ((err as any)?.response?.data?.message) {
        setError((err as any).response.data.message);
      }
      else if ((err as any)?.code === 'ECONNREFUSED') {
        setError('Cannot connect to server. Please try again later.');
      }
      else {
        setError('An error occurred during login. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        width: '100%',
        minHeight: '400px',
        padding: 0,
        margin: 0,
        backgroundColor: 'transparent',
      }}
    >
      <Paper
        elevation={3}
        sx={{
          padding: 4,
          width: 400,
          borderRadius: 2
        }}
      >
        {/* Logo/Header */}
        <Box sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          mb: 3
        }}>
          <NoteAlt color="primary" sx={{ fontSize: 40, mr: 1 }} />
          <Typography variant="h4" color="primary" fontWeight="bold">Notex</Typography>
        </Box>
        <Typography variant="h5" align="center" component="h2" sx={{ mb: 3 }}>
          Zaloguj się do swojego konta
        </Typography>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

        <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
          <TextField
            margin="normal"
            required
            fullWidth
            id="username"
            label="Username"
            name="username"
            type="text"
            autoComplete="username"
            autoFocus
            value={formData.username}
            onChange={handleChange}
            disabled={loading}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start"><Person /></InputAdornment>
              ),
            }}
            sx={{ mb: 2 }}
          />

          <TextField
            margin="normal"
            required
            fullWidth
            name="password"
            label="Hasło"
            type={showPassword ? 'text' : 'password'}
            id="password"
            autoComplete="current-password"
            value={formData.password}
            onChange={handleChange}
            disabled={loading}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start"><Lock /></InputAdornment>
              ),
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    aria-label="toggle password visibility"
                    onClick={() => setShowPassword(!showPassword)}
                    edge="end"
                  >
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
            sx={{ mb: 3 }}
          />

          <Button
            type="submit"
            fullWidth
            variant="contained"
            disabled={loading}
            sx={{ mt: 2, mb: 2, py: 1.5, fontSize: '1.1rem' }}
          >
            {loading ? (
              <>
                <CircularProgress size={20} sx={{ mr: 1 }} />
                Logowanie...
              </>
            ) : 'Zaloguj się'}
          </Button>

          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Typography variant="body2">
              Nie masz konta?{' '}
              <Button
                variant="text"
                onClick={() => navigate('/register')}
                disabled={loading}
                sx={{ textTransform: 'none' }}
              >
                Zarejestruj się
              </Button>
            </Typography>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default Login;
