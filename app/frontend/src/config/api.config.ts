// API Configuration
// Base paths match backend @ApplicationPath annotations
export const API_CONFIG = {
  // Use /api/* paths for both local dev (Vite proxy) and production (nginx proxy)
  USER_SERVICE_URL: import.meta.env.VITE_USER_SERVICE_URL || '/api/users',
  DOCS_SERVICE_URL: import.meta.env.VITE_DOCS_SERVICE_URL || '/api/docs',
  COMMENTS_SERVICE_URL: import.meta.env.VITE_COMMENTS_SERVICE_URL || '/api/comments',
  TOKEN_REFRESH_BUFFER: 60 * 1000, // Refresh token 60 seconds before expiry
};

// Local Storage Keys
export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'dockey_access_token',
  REFRESH_TOKEN: 'dockey_refresh_token',
  TOKEN_EXPIRY: 'dockey_token_expiry',
  USER: 'dockey_user',
};
