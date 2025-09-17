import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { notexAPI, apiHelpers } from '../services/api';

interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  isVerified: boolean;
  role: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const login = async (username: string, password: string) => {
    try {
      const response = await notexAPI.auth.login({ username, password });
      const { token } = response.data;
      
      apiHelpers.setToken(token);
      
      // Fetch user data after successful login
      const userResponse = await notexAPI.users.getMe();
      setUser(userResponse.data);
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  const logout = () => {
    apiHelpers.removeToken();
    setUser(null);
  };

  const isAuthenticated = !!user;

  // Check if user is logged in on app start
  useEffect(() => {
    const checkAuth = async () => {
      if (apiHelpers.isLoggedIn()) {
        try {
          console.log('Checking authentication...');
          const response = await notexAPI.users.getMe();
          console.log('User data received:', response.data);
          setUser(response.data);
        } catch (error) {
          console.error('Failed to fetch user data:', error);
          apiHelpers.removeToken();
          setUser(null);
        }
      } else {
        console.log('No token found, user not logged in');
        setUser(null);
      }
      setLoading(false);
    };

    checkAuth();
  }, []);

  const value: AuthContextType = {
    user,
    loading,
    login,
    logout,
    isAuthenticated,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
