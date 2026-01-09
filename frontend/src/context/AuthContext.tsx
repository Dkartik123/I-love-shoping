import { createContext, useContext, useEffect, useCallback, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { authApi } from '@/services/authApi';
import type { LoginRequest, RegisterRequest, AuthResponse, User } from '@/types';
import toast from 'react-hot-toast';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  requiresTwoFactor: boolean;
  login: (data: LoginRequest) => Promise<void>;
  loginWith2FA: (code: string) => Promise<void>;
  loginWithTokens: (accessToken: string, refreshToken: string) => void;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const {
    user,
    isAuthenticated,
    accessToken,
    refreshToken,
    requiresTwoFactor,
    tempEmail,
    setUser,
    setTokens,
    setRequiresTwoFactor,
    clearTwoFactor,
    logout: clearAuth,
  } = useAuthStore();

  // Handle successful authentication
  const handleAuthSuccess = useCallback(
    (response: AuthResponse) => {
      setTokens(response.accessToken, response.refreshToken);
      setUser(response.user);
      toast.success(`Welcome back, ${response.user.firstName}!`);
      navigate('/');
    },
    [setTokens, setUser, navigate]
  );

  // Login with email/password
  const login = useCallback(
    async (data: LoginRequest) => {
      const response = await authApi.login(data);

      if (response.requiresTwoFactor) {
        setRequiresTwoFactor(data.email);
        navigate('/2fa');
        return;
      }

      handleAuthSuccess(response);
    },
    [handleAuthSuccess, setRequiresTwoFactor, navigate]
  );

  // Login with 2FA code
  const loginWith2FA = useCallback(
    async (code: string) => {
      if (!tempEmail) {
        toast.error('Session expired. Please login again.');
        clearTwoFactor();
        navigate('/login');
        return;
      }

      const response = await authApi.verify2FA(tempEmail, code);
      handleAuthSuccess(response);
    },
    [tempEmail, handleAuthSuccess, clearTwoFactor, navigate]
  );

  // Login with OAuth2 tokens
  const loginWithTokens = useCallback(
    (accessToken: string, refreshTokenValue: string) => {
      setTokens(accessToken, refreshTokenValue);
      // Fetch user data
      authApi.getCurrentUser().then((userData) => {
        setUser(userData);
        toast.success(`Welcome, ${userData.firstName}!`);
      }).catch(() => {
        clearAuth();
      });
    },
    [setTokens, setUser, clearAuth]
  );

  // Register new user
  const register = useCallback(
    async (data: RegisterRequest) => {
      await authApi.register(data);
      toast.success('Registration successful! Please check your email to verify your account.');
      navigate('/login');
    },
    [navigate]
  );

  // Logout
  const logout = useCallback(async () => {
    try {
      if (refreshToken) {
        await authApi.logout(refreshToken);
      }
    } catch (error) {
      // Ignore logout errors
    } finally {
      clearAuth();
      toast.success('Logged out successfully');
      navigate('/login');
    }
  }, [refreshToken, clearAuth, navigate]);

  // Refresh user data
  const refreshUser = useCallback(async () => {
    if (!accessToken) return;

    try {
      const user = await authApi.getCurrentUser();
      setUser(user);
    } catch (error) {
      // If refresh fails, logout
      clearAuth();
    }
  }, [accessToken, setUser, clearAuth]);

  // Check auth state on mount
  useEffect(() => {
    if (refreshToken && !accessToken) {
      // Try to refresh the token on mount
      authApi
        .refreshTokens(refreshToken)
        .then((response) => {
          setTokens(response.accessToken, response.refreshToken);
          setUser(response.user);
        })
        .catch(() => {
          clearAuth();
        });
    }
  }, [refreshToken, accessToken, setTokens, setUser, clearAuth]);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading: false,
    requiresTwoFactor,
    login,
    loginWith2FA,
    loginWithTokens,
    register,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
