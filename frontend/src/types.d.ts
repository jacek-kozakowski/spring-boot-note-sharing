// Type declarations for JSX components
declare module '../components/Login' {
  import { FC } from 'react';
  const Login: FC;
  export default Login;
}

declare module '../components/Register' {
  import { FC } from 'react';
  const Register: FC;
  export default Register;
}

declare module '../context/AuthContext' {
  import { FC, ReactNode } from 'react';
  
  export interface AuthContextType {
    user: unknown;
    login: (credentials: unknown) => Promise<unknown>;
    logout: () => void;
    loading: boolean;
    isAuthenticated: boolean;
  }
  
  export const useAuth: () => AuthContextType;
  export const AuthProvider: FC<{ children: ReactNode }>;
}

// Global types for all .jsx files
declare module "*.jsx" {
  import { FC } from 'react';
  const Component: FC<unknown>;
  export default Component;
}