export type UserRole = 'USER' | 'ADMIN';

export interface User {
  id: number;
  keycloakId: string;
  username: string;
  email: string;
  fullName?: string;
  role: UserRole;
  createdAt: string;
  updatedAt?: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  role?: UserRole;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  userId: number;
  keycloakId: string;
  username: string;
  email: string;
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  message?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}
