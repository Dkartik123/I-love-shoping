import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { User } from '@/types';

interface AuthState {
  // User state
  user: User | null;
  isAuthenticated: boolean;
  
  // Token state - access token stored in memory only for security
  accessToken: string | null;
  refreshToken: string | null;
  
  // 2FA state
  requiresTwoFactor: boolean;
  tempEmail: string | null;
  
  // Actions
  setUser: (user: User) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  setRequiresTwoFactor: (email: string) => void;
  clearTwoFactor: () => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      // Initial state
      user: null,
      isAuthenticated: false,
      accessToken: null,
      refreshToken: null,
      requiresTwoFactor: false,
      tempEmail: null,

      // Set user after successful login
      setUser: (user) =>
        set({
          user,
          isAuthenticated: true,
          requiresTwoFactor: false,
          tempEmail: null,
        }),

      // Set tokens
      setTokens: (accessToken, refreshToken) =>
        set({
          accessToken,
          refreshToken,
        }),

      // Set 2FA requirement
      setRequiresTwoFactor: (email) =>
        set({
          requiresTwoFactor: true,
          tempEmail: email,
        }),

      // Clear 2FA state
      clearTwoFactor: () =>
        set({
          requiresTwoFactor: false,
          tempEmail: null,
        }),

      // Logout - clear all auth state
      logout: () =>
        set({
          user: null,
          isAuthenticated: false,
          accessToken: null,
          refreshToken: null,
          requiresTwoFactor: false,
          tempEmail: null,
        }),
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => sessionStorage),
      // Only persist refresh token, not access token
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated,
        refreshToken: state.refreshToken,
      }),
    }
  )
);
