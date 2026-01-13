import { graphqlClient, handleGraphQLError } from '../graphql.client';
import { GET_USERS, GET_USER_BY_ID } from './user.queries';

export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  createdAt: string;
}

interface GetUsersResponse {
  users: User[];
}

interface GetUserByIdResponse {
  userById: User;
}

/**
 * GraphQL User Service
 * Provides user data operations via GraphQL
 */
export const UserServiceGraphQL = {
  /**
   * Get all users
   */
  async getUsers(): Promise<User[]> {
    try {
      const data = await graphqlClient.request<GetUsersResponse>(GET_USERS);
      return data.users;
    } catch (error) {
      handleGraphQLError(error);
      throw error;
    }
  },

  /**
   * Get user by ID
   */
  async getUserById(id: string): Promise<User> {
    try {
      const data = await graphqlClient.request<GetUserByIdResponse>(GET_USER_BY_ID, { id });
      return data.userById;
    } catch (error) {
      handleGraphQLError(error);
      throw error;
    }
  },
};
