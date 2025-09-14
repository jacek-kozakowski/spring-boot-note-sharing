import React, {useState} from 'react';
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
import { Visibility, VisibilityOff, Person, Email, Lock, NoteAlt, CheckCircle} from '@mui/icons-material';

const Register = () => {
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
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const steps = ['Account Details', 'Email Verification']

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
        if (error) setError('');
    }

    const validateForm = () => {
        if (!formData.username || !formData.email || !formData.password || !formData.confirmPassword || !formData.firstName || !formData.lastName) {
            setError('Please fill in all fields');
            return false;
        }
        if (!formData.email.includes('@')) {
            setError('Please enter a valid email address');
            return false;
        }
        if (formData.password.length < 8) {
            setError('Password must be at least 8 characters long');
            return false;
        }
        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return false;
        }

        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault()
        if (!validateForm()) return;

        setLoading(true);
        setError('');
        setSuccess('');
        try{
            await notexAPI.auth.register({
                username: formData.username,
                email: formData.email,
                password: formData.password,
                firstName: formData.firstName,
                lastName: formData.lastName
            });
        }catch(err){
            if (err.response?.status === 409) {
                setError('User with this email already exists');
            } else if (err.response?.data?.message) {
                setError(err.response.data.message);
            } else if (err.code === 'ECONNREFUSED') {
                setError('Cannot connect to server.');
            } else {
                setError('Registration error occurred. Please try again.');
            }
        }finally {
            setLoading(false);
        }
    };

    const handleVerification = async (e) => {
        e.preventDefault();
        if (!verificationCode) {
            setError('Please enter the verification code');
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
            setSuccess('Account verified successfully! You can log in now.');
            setTimeout( () => navigate('/login'), 2000);
        }catch(err){
            if (err.response?.status === 400) {
                setError(err.response.data || 'Invalid verification code');
            } else if (err.response?.data?.message) {
                setError(err.response.data.message);
            } else {
                setError('Verification error occurred. Please try again.');
            }
        }finally {
            setLoading(false);
        }
    };

    const handleResendCode = async () => {
        setLoading(true);
        setError('');
        try{
            await notexAPI.auth.resend(formData.username);
            setSuccess('Verification code resent successfully!');
        }catch(err){
            if (err.response?.data?.message) {
                setError(err.response.data.message);
            } else {
                setError('Failed to resend verification code');
            }
        }finally {
            setLoading(false);
        }
    };

    const renderStepContent = (step) => {
        switch (step) {
            case 0:
                return (
                    <Box component="form" onSubmit={handleSubmit} sx={{width: '100%'}}>
                        <TextField
                            margin="normal"
                            required
                            fullwidth
                            id = "username"
                            label = "Username"
                            name = "username"
                            type= "text"
                            autoComplete = "username"
                            autoFocus
                            value = {formData.username}
                            onChange = {handleInputChange}
                            disabled = {loading}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <Person />
                                    </InputAdornment>
                                )
                            }}
                            sx ={{mb:2}}
                        />

                        <TextField
                            margin="normal"
                            required
                            fullwidth
                            id = "email"
                            label = "Email"
                            name = "email"
                            type= "email"
                            autoComplete = "email"
                            value = {formData.email}
                            onChange = {handleInputChange}
                            disabled = {loading}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <Email />
                                    </InputAdornment>
                                )
                            }}
                            sx ={{mb:2}}
                        />

                        <TextField
                            margin="normal"
                            required
                            fullwidth
                            id = "password"
                            label = "Password"
                            name = "password"
                            type= {showPassword ? 'text' : 'password'}
                            autoComplete = "new-password"
                            value = {formData.password}
                            onChange = {handleInputChange}
                            disabled = {loading}
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
                            sx ={{mb:2}}
                        />
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            name="confirmPassword"
                            label="Confirm Password"
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
                            fullwidth
                            id = "firstName"
                            label = "First Name"
                            name = "firstName"
                            type= "text"
                            autoComplete = "firstName"
                            value = {formData.firstName}
                            onChange = {handleInputChange}
                            disabled = {loading}
                            sx ={{mb:2}}
                        />

                        <TextField
                            margin="normal"
                            required
                            fullwidth
                            id = "lastName"
                            label = "Last Name"
                            name = "lastName"
                            type= "text"
                            autoComplete = "lastName"
                            value = {formData.lastName}
                            onChange = {handleInputChange}
                            disabled = {loading}
                            sx ={{mb:2}}
                        />

                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            disabled = {loading}
                            sx={{mt: 2, mb: 2, py:1.5, fontSize: '1.1rem'}}
                        >
                            {loading ?(
                            <>
                                <CircularProgress size={20}  sx = {{mr: 1}}/>
                                Registering...
                            </>
                            ) : 'Register'}
                        </Button>
                    </Box>
                        );
            case 1:
                return (
                    <Box component="form" onSubmit={handleVerification} sx={{width: '100%'}}>
                        <Typography variant="body1" sx={{mb: 3, textAlign: 'center'}}>
                            A verification code has been sent to <strong>{formData.email}</strong>. Please enter it below to verify your account.
                        </Typography>

                        <TextField
                            margin="normal"
                            required
                            fullwidth
                            id = "verificationCode"
                            label = "Verification Code"
                            name = "verificationCode"
                            type= "text"
                            autoComplete = "off"
                            autoFocus
                            value = {verificationCode}
                            onChange = {e => setVerificationCode(e.target.value)}
                            disabled = {loading}
                            sx = {{ mb: 3 }}
                        />

                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            disabled = {loading}
                            sx={{mt: 2, mb: 2, py:1.5, fontSize: '1.1rem'}}
                        >
                            {loading ? (
                                <>
                                    <CircularProgress size={20} sx={{mr: 1}} />
                                    Verifying...
                                </>
                            ) : 'Verify Account'}

                        </Button>

                        <Button
                            fullWidth
                            variant="contained"
                            onClick={handleResendCode}
                            disabled = {loading}
                            sx={{mb: 2}}
                        >
                            Resend Verification Code
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
                width: '100vw',
                height: '100vh',
                backgroundColor: '#f5f5f5'
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
                    Create Your Account
                </Typography>

                {/* Stepper */}
                <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
                    {steps.map((label) =>{
                        <Step key={label}>
                            <StepLabel>{label}</StepLabel>
                        </Step>
                    })}
                </Stepper>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
                {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

                {renderStepContent(activeStep)}
                <Box sx={{textAlign: 'center', mt: 2}}>
                    <Typography variant="body2" >
                        Already have an account?{' '}
                        <Button
                            variant="text"
                            onClick={() => navigate('/login')}
                            disabled={loading}
                            sx={{ textTransform: 'none' }}
                        >
                            Login
                        </Button>
                    </Typography>

                </Box>
            </Paper>
        </Box>
    )
}


export default Register;