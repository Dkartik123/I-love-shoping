import api from '@/lib/api';
import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from '@/types';

export const authApi = {
  // Login with email/password
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', data);
    return response.data.data;
  },

  // Register new user
  register: async (data: RegisterRequest): Promise<User> => {
    const response = await api.post<ApiResponse<User>>('/auth/register', data);
    return response.data.data;
  },

  // Logout
  logout: async (refreshToken: string): Promise<void> => {
    await api.post('/auth/logout', { refreshToken });
  },

  // Refresh tokens
  refreshTokens: async (refreshToken: string): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/refresh', {
      refreshToken,
    });
    return response.data.data;
  },

  // Get current user
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<ApiResponse<User>>('/auth/me');
    return response.data.data;
  },

  // Forgot password
  forgotPassword: async (email: string): Promise<void> => {
    await api.post('/auth/forgot-password', { email });
  },

  // Reset password
  resetPassword: async (token: string, password: string, confirmPassword: string): Promise<void> => {
    await api.post('/auth/reset-password', { token, password, confirmPassword });
  },

  // Verify 2FA code
  verify2FA: async (email: string, code: string): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/2fa/verify', {
      email,
      code,
    });
    return response.data.data;
  },

  // Enable 2FA
  enable2FA: async (): Promise<{ secret: string; qrCodeUrl: string }> => {
    const response = await api.post<ApiResponse<{ secret: string; qrCodeUrl: string }>>(
      '/auth/2fa/enable'
    );
    return response.data.data;
  },

  // Confirm 2FA setup
  confirm2FA: async (code: string): Promise<{ backupCodes: string[] }> => {
    const response = await api.post<ApiResponse<{ backupCodes: string[] }>>(
      '/auth/2fa/confirm',
      { code }
    );
    return response.data.data;
  },

  // Disable 2FA
  disable2FA: async (password: string): Promise<void> => {
    await api.post('/auth/2fa/disable', { password });
  },

  // Verify email
  verifyEmail: async (token: string): Promise<void> => {
    await api.get(`/auth/verify-email?token=${token}`);
  },

  // Resend verification email
  resendVerificationEmail: async (): Promise<void> => {
    await api.post('/auth/resend-verification');
  },

  // OAuth2 login URLs
  getOAuth2Url: (provider: 'google' | 'facebook'): string => {
    const baseUrl = import.meta.env.VITE_API_URL || '';
    return `${baseUrl}/oauth2/authorize/${provider}`;
  },
};
