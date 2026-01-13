import { userServiceApi, setTokens, clearTokens } from './api.client';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  User,
  RefreshTokenRequest,
  RefreshTokenResponse,
} from '../types';
import { STORAGE_KEYS } from '../config/api.config';

export const authService = {
  /**
   * Register a new user
   */
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await userServiceApi.post<AuthResponse>('/users/register', data);
    const authData = response.data;
    
    // Store tokens and user data
    setTokens(authData.accessToken, authData.refreshToken, authData.expiresIn);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify({
      id: authData.userId,
      keycloakId: authData.keycloakId,
      username: authData.username,
      email: authData.email,
    }));
    
    return authData;
  },

  /**
   * Login user
   */
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await userServiceApi.post<AuthResponse>('/users/login', data);
    const authData = response.data;
    
    // Store tokens and user data
    setTokens(authData.accessToken, authData.refreshToken, authData.expiresIn);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify({
      id: authData.userId,
      keycloakId: authData.keycloakId,
      username: authData.username,
      email: authData.email,
    }));
    
    return authData;
  },

  /**
   * Refresh access token
   */
  async refresh(refreshToken: string): Promise<RefreshTokenResponse> {
    const response = await userServiceApi.post<RefreshTokenResponse>(
      '/users/refresh',
      { refreshToken } as RefreshTokenRequest
    );
    const data = response.data;
    setTokens(data.accessToken, data.refreshToken, data.expiresIn);
    return data;
  },

  /**
   * Logout user
   */
  logout(): void {
    clearTokens();
  },

  /**
   * Get current user from storage
   */
  getCurrentUserFromStorage(): Partial<User> | null {
    const userStr = localStorage.getItem(STORAGE_KEYS.USER);
    if (!userStr) return null;
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    return !!token;
  },
};
