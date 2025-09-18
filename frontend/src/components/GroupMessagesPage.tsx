import React, { useState, useEffect, useCallback } from 'react';
import {
  Container,
  Typography,
  Box,
  IconButton,
  AppBar,
  Toolbar,
  Avatar,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  ArrowBack as BackIcon,
  People as PeopleIcon,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { notexAPI } from '../services/api';
import MessageList from './MessageList';
import MessageInput from './MessageInput';
import GroupMembersDialog from './GroupMembersDialog';
import type { Group } from '../types/group';
import type { Message, MessagePage } from '../types/message';

const GroupMessagesPage: React.FC = () => {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  
  const [group, setGroup] = useState<Group | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [membersDialogOpen, setMembersDialogOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  const loadGroup = useCallback(async () => {
    if (!groupId) return;

    try {
      const response = await notexAPI.groups.getGroupById(Number(groupId));
      setGroup(response.data);
    } catch (err: unknown) {
      if ((err as any)?.response?.status === 403) {
        setError('You are not a member of this group. Please join the group to access messages.');
      } else {
        setError('Failed to load group');
      }
      console.error('Error loading group:', err);
    }
  }, [groupId]);

  const loadMessages = useCallback(async (page: number = 0, append: boolean = false) => {
    if (!groupId) return;

    try {
      if (page === 0) {
        setLoading(true);
      } else {
        setLoadingMore(true);
      }
      setError(null);

      const response = await notexAPI.messages.getMessages(
        Number(groupId),
        page,
        20
      );
      
      const messagePage: MessagePage = response.data;
      
      if (append) {
        setMessages(prev => [...messagePage.content, ...prev]);
      } else {
        setMessages(messagePage.content);
      }
      
      setHasMore(!messagePage.last);
      setCurrentPage(page);
    } catch (err: unknown) {
      if ((err as any)?.response?.status === 403) {
        setError('You are not a member of this group. Please join the group to access messages.');
      } else {
        setError('Failed to load messages');
      }
      console.error('Error loading messages:', err);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, [groupId]);

  useEffect(() => {
    loadGroup();
  }, [loadGroup]);

  useEffect(() => {
    loadMessages(0, false);
  }, [loadMessages]);

  const handleSendMessage = useCallback(async (messageData: { content: string }) => {
    if (!groupId) return;

    try {
      setSending(true);
      setError(null);
      
      await notexAPI.messages.sendMessage(Number(groupId), messageData);
      
      // Reload messages to get the new one
      await loadMessages(0, false);
    } catch (err: unknown) {
      if ((err as any)?.response?.status === 403) {
        setError('You are not a member of this group. Cannot send messages.');
      } else if ((err as any)?.response?.data?.message) {
        setError(`Failed to send message: ${(err as any).response.data.message}`);
      } else {
        setError('Failed to send message');
      }
      console.error('Error sending message:', err);
    } finally {
      setSending(false);
    }
  }, [groupId, loadMessages]);

  const handleLoadMore = useCallback(() => {
    if (hasMore && !loadingMore) {
      loadMessages(currentPage + 1, true);
    }
  }, [hasMore, loadingMore, loadMessages, currentPage]);


  if (!groupId) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">Invalid group identifier</Alert>
      </Container>
    );
  }

  if (loading && !group) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box display="flex" justifyContent="center">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error && !group) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Box sx={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="static" elevation={0} sx={{ bgcolor: 'white', color: 'text.primary' }}>
        <Toolbar>
          <IconButton
            edge="start"
            color="inherit"
            onClick={() => navigate('/groups')}
            sx={{ mr: 2 }}
          >
            <BackIcon />
          </IconButton>
          
          <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
            {group?.name?.charAt(0).toUpperCase() || 'G'}
          </Avatar>
          
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6" component="div">
              {group?.name || 'Untitled Group'}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {group?.membersCount || 0} member{(group?.membersCount || 0) !== 1 ? 's' : ''}
            </Typography>
          </Box>
          
          <IconButton
            color="inherit"
            onClick={() => setMembersDialogOpen(true)}
          >
            <PeopleIcon />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        {error && (
          <Alert severity="error" sx={{ m: 2 }}>
            {error}
          </Alert>
        )}

        <MessageList
          messages={messages}
          loading={loading}
          error={null}
          onLoadMore={handleLoadMore}
          hasMore={hasMore}
        />
      </Box>

      <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
        <MessageInput
          onSendMessage={handleSendMessage}
          disabled={sending}
          placeholder={`Write a message in ${group?.name || 'group'}...`}
        />
      </Box>

      <GroupMembersDialog
        open={membersDialogOpen}
        group={group}
        onClose={() => setMembersDialogOpen(false)}
        onMemberRemoved={loadGroup}
      />
    </Box>
  );
};

export default GroupMessagesPage;
