import { userServiceApi } from './api.client';
import type { User } from '../types';

export const userService = {
  /**
   * Get current user profile
   */
  async getCurrentUser(): Promise<User> {
    const response = await userServiceApi.get<User>('/users/me');
    return response.data;
  },

  /**
   * Get all users
   */
  async getAllUsers(): Promise<User[]> {
    const response = await userServiceApi.get<User[]>('/users');
    return response.data;
  },

  /**
   * Get user by ID
   */
  async getUserById(id: number): Promise<User> {
    const response = await userServiceApi.get<User>(`/users/${id}`);
    return response.data;
  },

  /**
   * Get user by email
   */
  async getUserByEmail(email: string): Promise<User> {
    const response = await userServiceApi.get<User>(`/users/email/${encodeURIComponent(email)}`);
    return response.data;
  },

  /**
   * Update user
   */
  async updateUser(id: number, data: Partial<User>): Promise<User> {
    const response = await userServiceApi.put<User>(`/users/${id}`, data);
    return response.data;
  },

  /**
   * Delete user
   */
  async deleteUser(id: number): Promise<void> {
    await userServiceApi.delete(`/users/${id}`);
  },

  /**
   * Delete current user account
   */
  async deleteCurrentUser(): Promise<void> {
    await userServiceApi.delete('/users/me');
  },
};
