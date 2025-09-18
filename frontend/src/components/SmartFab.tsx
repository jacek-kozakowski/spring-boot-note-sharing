import React from 'react';
import { Fab, Tooltip } from '@mui/material';
import { Add as AddIcon, GroupAdd as GroupAddIcon } from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';

const SmartFab: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const getFabConfig = () => {
    if (location.pathname === '/groups') {
      return {
        icon: <GroupAddIcon />,
        tooltip: 'Create Group',
        onClick: () => {
          // This will be handled by the parent component
          const event = new CustomEvent('fab-click', { detail: { action: 'create-group' } });
          window.dispatchEvent(event);
        }
      };
    } else {
      return {
        icon: <AddIcon />,
        tooltip: 'Create Note',
        onClick: () => navigate('/notes/create')
      };
    }
  };

  const config = getFabConfig();

  return (
    <Tooltip title={config.tooltip} placement="left">
      <Fab
        color="primary"
        aria-label={config.tooltip.toLowerCase()}
        sx={{
          position: 'fixed',
          bottom: 16,
          right: 16,
          zIndex: 1000,
        }}
        onClick={config.onClick}
      >
        {config.icon}
      </Fab>
    </Tooltip>
  );
};

export default SmartFab;

