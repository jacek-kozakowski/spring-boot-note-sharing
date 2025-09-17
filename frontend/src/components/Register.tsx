import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { notexAPI } from '../services/api';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Alert,
  InputAdornment,
  IconButton,
  CircularProgress,
  Stepper,
  Step,
  StepLabel
} from '@mui/material';
import { Visibility, VisibilityOff, Person, Email, Lock, NoteAlt } from '@mui/icons-material';

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [activeStep, setActiveStep] = useState(0);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: ''
  });
  const [verificationCode, setVerificationCode] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const steps = ['Dane Konta', 'Weryfikacja Email'];

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
    if (error) setError('');
  };

  const validateForm = () => {
    if (!formData.username || !formData.email || !formData.password || !formData.confirmPassword || !formData.firstName || !formData.lastName) {
      setError('Proszę wypełnić wszystkie pola');
      return false;
    }
    if (!formData.email.includes('@')) {
      setError('Proszę podać prawidłowy adres email');
      return false;
    }
    if (formData.password.length < 8) {
      setError('Hasło musi mieć co najmniej 8 znaków');
      return false;
    }
    if (formData.password !== formData.confirmPassword) {
      setError('Hasła nie są identyczne');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    setError('');
    setSuccess('');
    try {
      await notexAPI.auth.register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
        firstName: formData.firstName,
        lastName: formData.lastName
      });
      setSuccess('Rejestracja zakończona pomyślnie! Sprawdź email z kodem weryfikacyjnym.');
      setActiveStep(1);
    } catch (err: any) {
      if (err.response?.status === 409) {
        setError('Użytkownik z tym emailem już istnieje');
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.code === 'ECONNREFUSED') {
        setError('Nie można połączyć się z serwerem.');
      } else {
        setError('Wystąpił błąd rejestracji. Spróbuj ponownie.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleVerification = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!verificationCode) {
      setError('Proszę podać kod weryfikacyjny');
      return;
    }
    setLoading(true);
    setError('');
    setSuccess('');
    try {
      await notexAPI.auth.verify({
        username: formData.username,
        verificationCode: verificationCode
      });
      setSuccess('Konto zweryfikowane pomyślnie! Możesz się teraz zalogować.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      if (err.response?.status === 400) {
        setError(err.response.data || 'Nieprawidłowy kod weryfikacyjny');
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Wystąpił błąd weryfikacji. Spróbuj ponownie.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResendCode = async () => {
    setLoading(true);
    setError('');
    try {
      await notexAPI.auth.resend({ username: formData.username });
      setSuccess('Kod weryfikacyjny został ponownie wysłany!');
    } catch (err: any) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Nie udało się ponownie wysłać kodu weryfikacyjnego');
      }
    } finally {
      setLoading(false);
    }
  };

  const renderStepContent = (step: number) => {
    switch (step) {
      case 0:
        return (
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
              onChange={handleInputChange}
              disabled={loading}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Person />
                  </InputAdornment>
                )
              }}
              sx={{ mb: 2 }}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              id="email"
              label="Email"
              name="email"
              type="email"
              autoComplete="email"
              value={formData.email}
              onChange={handleInputChange}
              disabled={loading}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Email />
                  </InputAdornment>
                )
              }}
              sx={{ mb: 2 }}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              id="password"
              label="Hasło"
              name="password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="new-password"
              value={formData.password}
              onChange={handleInputChange}
              disabled={loading}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Lock />
                  </InputAdornment>
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
              sx={{ mb: 2 }}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="confirmPassword"
              label="Potwierdź hasło"
              type={showConfirmPassword ? 'text' : 'password'}
              id="confirmPassword"
              autoComplete="new-password"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              disabled={loading}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start"><Lock /></InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle confirm password visibility"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      edge="end"
                    >
                      {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 3 }}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              id="firstName"
              label="Imię"
              name="firstName"
              type="text"
              autoComplete="firstName"
              value={formData.firstName}
              onChange={handleInputChange}
              disabled={loading}
              sx={{ mb: 2 }}
            />

            <TextField
              margin="normal"
              required
              fullWidth
              id="lastName"
              label="Nazwisko"
              name="lastName"
              type="text"
              autoComplete="lastName"
              value={formData.lastName}
              onChange={handleInputChange}
              disabled={loading}
              sx={{ mb: 2 }}
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
                  Rejestrowanie...
                </>
              ) : 'Zarejestruj się'}
            </Button>
          </Box>
        );
      case 1:
        return (
          <Box component="form" onSubmit={handleVerification} sx={{ width: '100%' }}>
            <Typography variant="body1" sx={{ mb: 3, textAlign: 'center' }}>
              Kod weryfikacyjny został wysłany na <strong>{formData.email}</strong>. Wprowadź go poniżej, aby zweryfikować konto.
            </Typography>

            <TextField
              margin="normal"
              required
              fullWidth
              id="verificationCode"
              label="Kod weryfikacyjny"
              name="verificationCode"
              type="text"
              autoComplete="off"
              autoFocus
              value={verificationCode}
              onChange={e => setVerificationCode(e.target.value)}
              disabled={loading}
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
                  Weryfikowanie...
                </>
              ) : 'Zweryfikuj konto'}

            </Button>

            <Button
              fullWidth
              variant="contained"
              onClick={handleResendCode}
              disabled={loading}
              sx={{ mb: 2 }}
            >
              Wyślij ponownie kod weryfikacyjny
            </Button>
          </Box>
        );

      default:
        return null;
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
        backgroundColor: 'transparent'
      }}
    >
      <Paper
        elevation={3}
        sx={{
          maxWidth: 450,
          padding: 4,
          borderRadius: 2,
          boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)'
        }}
      >
        {/* Logo/Header */}
        <Box sx={{ textAlign: 'center', mb: 4 }}>
          <NoteAlt sx={{ fontSize: 48, color: 'primary.main', mr: 1 }} />
          <Typography variant="h4" component="h1" sx={{ mb: 2 }}>
            Notex
          </Typography>
        </Box>

        <Typography variant="h5" component="h2" align="center" sx={{ mb: 3 }}>
          Create your account
        </Typography>

        {/* Stepper */}
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

        {renderStepContent(activeStep)}
        <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Typography variant="body2">
              Masz już konto?{' '}
              <Button
                variant="text"
                onClick={() => navigate('/login')}
                disabled={loading}
                sx={{ textTransform: 'none' }}
              >
                Zaloguj się
              </Button>
            </Typography>

        </Box>
      </Paper>
    </Box>
  );
};

export default Register;
