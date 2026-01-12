import { userServiceApi } from './api.client';
import type { User } from '../types';

export const userService = {
  /**
   * Get current user profile
   */
  async getCurrentUser(): Promise<User> {
    const response = await userServiceApi.get<User>('/v1/users/me');
    return response.data;
  },

  /**
   * Get all users
   */
  async getAllUsers(): Promise<User[]> {
    const response = await userServiceApi.get<User[]>('/v1/users');
    return response.data;
  },

  /**
   * Get user by ID
   */
  async getUserById(id: number): Promise<User> {
    const response = await userServiceApi.get<User>(`/v1/users/${id}`);
    return response.data;
  },

  /**
   * Get user by email
   */
  async getUserByEmail(email: string): Promise<User> {
    const response = await userServiceApi.get<User>(`/v1/users/email/${encodeURIComponent(email)}`);
    return response.data;
  },

  /**
   * Update user
   */
  async updateUser(id: number, data: Partial<User>): Promise<User> {
    const response = await userServiceApi.put<User>(`/v1/users/${id}`, data);
    return response.data;
  },

  /**
   * Delete user
   */
  async deleteUser(id: number): Promise<void> {
    await userServiceApi.delete(`/v1/users/${id}`);
  },

  /**
   * Delete current user account
   */
  async deleteCurrentUser(): Promise<void> {
    await userServiceApi.delete('/v1/users/me');
  },
};
