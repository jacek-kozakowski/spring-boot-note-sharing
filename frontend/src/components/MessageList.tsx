import React, { useEffect, useRef } from 'react';
import {
  Box,
  List,
  ListItem,
  Typography,
  Avatar,
  Paper,
  CircularProgress,
  Alert,
  Button,
} from '@mui/material';
import { useAuth } from '../context/AuthContext';
import type { Message } from '../types/message';

interface MessageListProps {
  messages: Message[];
  loading: boolean;
  error: string | null;
  onLoadMore?: () => void;
  hasMore?: boolean;
}

const MessageList: React.FC<MessageListProps> = ({
  messages,
  loading,
  error,
  onLoadMore,
  hasMore = false,
}) => {
  const { user } = useAuth();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const formatTime = (dateString: string | undefined) => {
    if (!dateString) return '--:--';
    return new Date(dateString).toLocaleTimeString('pl-PL', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatDate = (dateString: string | undefined) => {
    if (!dateString) return 'Unknown date';
    return new Date(dateString).toLocaleDateString('pl-PL', {
      day: 'numeric',
      month: 'short',
    });
  };

  const isOwnMessage = (message: Message) => {
    return message.author === user?.username;
  };

  const getInitials = (author: string) => {
    const parts = author.split(' ');
    const first = parts[0]?.charAt(0) || 'U';
    const last = parts[1]?.charAt(0) || 'N';
    return `${first}${last}`.toUpperCase();
  };

  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        {error}
      </Alert>
    );
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <List sx={{ flexGrow: 1, overflow: 'auto', p: 1 }}>
        {loading && messages.length === 0 && (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
            <CircularProgress />
          </Box>
        )}

        {messages.map((message, index) => {
          const isOwn = isOwnMessage(message);
          const showDate = index === 0 || 
            new Date(message.createdAt).toDateString() !== 
            new Date(messages[index - 1].createdAt).toDateString();

          return (
            <React.Fragment key={message.id.toString()}>
              {showDate && (
                <Box sx={{ textAlign: 'center', my: 1 }}>
                  <Typography variant="caption" color="text.secondary">
                    {formatDate(message.createdAt)}
                  </Typography>
                </Box>
              )}
              
              <ListItem
                sx={{
                  justifyContent: isOwn ? 'flex-end' : 'flex-start',
                  alignItems: 'flex-start',
                  px: 1,
                }}
              >
                <Box
                  sx={{
                    display: 'flex',
                    flexDirection: isOwn ? 'row-reverse' : 'row',
                    alignItems: 'flex-start',
                    maxWidth: '70%',
                    gap: 1,
                  }}
                >
                  {!isOwn && (
                    <Avatar
                      sx={{
                        width: 32,
                        height: 32,
                        bgcolor: 'primary.main',
                        fontSize: '0.75rem',
                      }}
                    >
                      {getInitials(message.author)}
                    </Avatar>
                  )}
                  
                  <Paper
                    elevation={1}
                    sx={{
                      p: 1.5,
                      bgcolor: isOwn ? 'primary.main' : 'grey.100',
                      color: isOwn ? 'white' : 'text.primary',
                      borderRadius: 2,
                      position: 'relative',
                    }}
                  >
                    {!isOwn && (
                      <Typography
                        variant="caption"
                        sx={{
                          display: 'block',
                          fontWeight: 600,
                          mb: 0.5,
                          color: isOwn ? 'white' : 'text.secondary',
                        }}
                      >
                        {message.author || 'Unknown User'}
                      </Typography>
                    )}
                    
                    <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                      {message.content || 'No content'}
                    </Typography>
                    
                    <Typography
                      variant="caption"
                      sx={{
                        display: 'block',
                        mt: 0.5,
                        opacity: 0.7,
                        textAlign: 'right',
                      }}
                    >
                      {formatTime(message.createdAt)}
                    </Typography>
                  </Paper>
                </Box>
              </ListItem>
            </React.Fragment>
          );
        })}
        
        {hasMore && (
          <Box sx={{ textAlign: 'center', p: 2 }}>
            <Button
              variant="outlined"
              size="small"
              onClick={onLoadMore}
              disabled={loading}
            >
              {loading ? 'Loading...' : 'Load more'}
            </Button>
          </Box>
        )}
        
        <div ref={messagesEndRef} />
      </List>
    </Box>
  );
};

export default MessageList;
