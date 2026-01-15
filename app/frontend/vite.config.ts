import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // User service GraphQL - KumuluzEE GraphQL is at root /graphql
      '/api/users/graphql': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api\/users\/graphql/, '/graphql'),
      },
      // User service routes - backend has @ApplicationPath("/api/users")
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
      },
      // Docs service routes - backend has @ApplicationPath("/api/docs")
      '/api/docs': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // Comments service routes - backend has @ApplicationPath("/api/comments")
      '/api/comments': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
