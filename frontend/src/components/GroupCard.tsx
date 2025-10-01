import React from 'react';
import {
  Card,
  CardContent,
  CardActions,
  Typography,
  Button,
  Box,
  Chip,
  IconButton,
  Avatar,
  Divider,
} from '@mui/material';
import {
  People as PeopleIcon,
  AccessTime as TimeIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Message as MessageIcon,
  ExitToApp as LeaveIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import type { Group } from '../types/group';

interface GroupCardProps {
  group: Group;
  isOwner: boolean;
  isMember?: boolean;
  onEdit?: (group: Group) => void;
  onDelete?: (group: Group) => void;
  onLeave?: (group: Group) => void;
  onJoin?: (group: Group) => void;
}

const GroupCard: React.FC<GroupCardProps> = ({
  group,
  isOwner,
  isMember = false,
  onEdit,
  onDelete,
  onLeave,
  onJoin,
}) => {
  const navigate = useNavigate();

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pl-PL', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleViewMessages = () => {
    navigate(`/groups/${group.id}/messages`);
  };

  const handleEdit = () => {
    if (onEdit) {
      onEdit(group);
    }
  };

  const handleDelete = () => {
    if (onDelete) {
      onDelete(group);
    }
  };

  const handleLeave = () => {
    if (onLeave) {
      onLeave(group);
    }
  };

  return (
    <Card
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        transition: 'transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: 4,
        },
      }}
    >
      <CardContent sx={{ flexGrow: 1 }}>
        <Box sx={{ mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
            <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
              {group.name?.charAt(0).toUpperCase() || 'G'}
            </Avatar>
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="h6" component="h2" noWrap>
                {group.name || 'No name'}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Owner: {group.ownerUsername || 'Unknown'}
              </Typography>
            </Box>
          </Box>
          
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
            <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
              ID: {group.id}
            </Typography>
            {isOwner && (
              <Chip label="Owner" color="primary" size="small" />
            )}
          </Box>
        </Box>

        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            mb: 2,
            display: '-webkit-box',
            WebkitLineClamp: 3,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
          }}
        >
          {group.description || 'No description'}
        </Typography>

        <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
          <PeopleIcon sx={{ fontSize: 16, mr: 0.5, color: 'text.secondary' }} />
          <Typography variant="caption" color="text.secondary">
            {group.membersCount || 0} members
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <TimeIcon sx={{ fontSize: 16, mr: 0.5, color: 'text.secondary' }} />
          <Typography variant="caption" color="text.secondary">
            Created: {group.createdAt ? formatDate(group.createdAt) : 'Unknown date'}
          </Typography>
        </Box>
      </CardContent>

      <Divider />

      <CardActions sx={{ justifyContent: 'space-between', p: 2 }}>
        <Button
          size="small"
          startIcon={<MessageIcon />}
          onClick={handleViewMessages}
          variant="contained"
        >
          Messages
        </Button>

        <Box>
          {isOwner && (
            <>
              <IconButton
                size="small"
                onClick={handleEdit}
                color="primary"
                title="Edit group"
              >
                <EditIcon />
              </IconButton>
              <IconButton
                size="small"
                onClick={handleDelete}
                color="error"
                title="Delete group"
              >
                <DeleteIcon />
              </IconButton>
            </>
          )}
          {!isOwner && isMember && (
            <IconButton
              size="small"
              onClick={handleLeave}
              color="error"
              title="Leave group"
            >
              <LeaveIcon />
            </IconButton>
          )}
          {!isOwner && !isMember && onJoin && (
            <Button
              size="small"
              onClick={() => onJoin(group)}
              color="primary"
              variant="outlined"
            >
              Join
            </Button>
          )}
        </Box>
      </CardActions>
    </Card>
  );
};

export default GroupCard;
