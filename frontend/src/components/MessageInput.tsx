import React, { useState, useRef, useEffect } from 'react';
import {
  TextField,
  IconButton,
  Paper,
} from '@mui/material';
import {
  Send as SendIcon,
} from '@mui/icons-material';

interface MessageInputProps {
  onSendMessage: (message: { content: string }) => void;
  disabled?: boolean;
  placeholder?: string;
}

const MessageInput: React.FC<MessageInputProps> = ({
  onSendMessage,
  disabled = false,
  placeholder = 'Napisz wiadomość...',
}) => {
  const [message, setMessage] = useState('');
  const textFieldRef = useRef<HTMLInputElement>(null);

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    
    if (message.trim() && !disabled) {
      onSendMessage({ content: message.trim() });
      setMessage('');
    }
  };

  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleSubmit(event);
    }
  };

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setMessage(event.target.value);
  };

  useEffect(() => {
    if (!disabled) {
      textFieldRef.current?.focus();
    }
  }, [disabled]);

  return (
    <Paper
      component="form"
      onSubmit={handleSubmit}
      sx={{
        p: 1,
        display: 'flex',
        alignItems: 'flex-end',
        gap: 1,
        borderRadius: 2,
        border: '1px solid',
        borderColor: 'divider',
      }}
    >
      <TextField
        ref={textFieldRef}
        fullWidth
        multiline
        maxRows={4}
        value={message}
        onChange={handleChange}
        onKeyPress={handleKeyPress}
        placeholder={placeholder}
        disabled={disabled}
        variant="outlined"
        size="small"
        sx={{
          '& .MuiOutlinedInput-root': {
            border: 'none',
            '& fieldset': {
              border: 'none',
            },
          },
        }}
      />
      
      <IconButton
        type="submit"
        color="primary"
        disabled={disabled || !message.trim()}
        sx={{
          alignSelf: 'flex-end',
          mb: 0.5,
        }}
      >
        <SendIcon />
      </IconButton>
    </Paper>
  );
};

export default MessageInput;
