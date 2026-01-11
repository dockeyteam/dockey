import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/v1/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
      },
      '/v1/documents': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/v1/comments': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
