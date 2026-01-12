# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source files (including .env files)
COPY . .

# Build argument for environment (k8s, docker, or default)
ARG BUILD_ENV=production

# Copy appropriate .env file if it exists
RUN if [ -f ".env.${BUILD_ENV}" ]; then cp ".env.${BUILD_ENV}" .env.production; fi

# Build the application (Vite uses .env.production during build)
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy built assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
