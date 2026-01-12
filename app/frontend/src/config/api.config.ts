// API Configuration
export const API_CONFIG = {
  // Use empty string for proxy mode (local dev), full URLs for Docker
  USER_SERVICE_URL: import.meta.env.VITE_USER_SERVICE_URL || '',
  DOCS_SERVICE_URL: import.meta.env.VITE_DOCS_SERVICE_URL || '',
  COMMENTS_SERVICE_URL: import.meta.env.VITE_COMMENTS_SERVICE_URL || '',
  TOKEN_REFRESH_BUFFER: 60 * 1000, // Refresh token 60 seconds before expiry
};

// Local Storage Keys
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'dockey_access_token',
  REFRESH_TOKEN: 'dockey_refresh_token',
  TOKEN_EXPIRY: 'dockey_token_expiry',
  USER: 'dockey_user',
};
