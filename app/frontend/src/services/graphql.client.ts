import { GraphQLClient } from 'graphql-request';
import { API_CONFIG } from '../config/api.config';

// GraphQL client for user-service
// GraphQLClient requires absolute URL, so we prepend origin for relative paths
const getAbsoluteUrl = (path: string): string => {
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  // Use window.location.origin to create absolute URL from relative path
  return `${window.location.origin}${path}`;
};

const graphqlEndpoint = getAbsoluteUrl(`${API_CONFIG.USER_SERVICE_URL}/graphql`);

export const graphqlClient = new GraphQLClient(graphqlEndpoint, {
  headers: (): Record<string, string> => {
    const token = localStorage.getItem('dockey_access_token');
    if (token) {
      return { Authorization: `Bearer ${token}` };
    }
    return {};
  },
});

// Helper to handle GraphQL errors
export const handleGraphQLError = (error: any) => {
  if (error.response?.errors) {
    const firstError = error.response.errors[0];
    throw new Error(firstError.message || 'GraphQL request failed');
  }
  throw error;
};
