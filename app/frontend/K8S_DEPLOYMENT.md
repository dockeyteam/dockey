# Dockey Frontend - Kubernetes Deployment Guide

## Overview

The frontend is a React + TypeScript application built with Vite, served by Nginx in production. This guide covers deploying it to Kubernetes with the Dockey Helm chart.

## Architecture

```
┌──────────────────────────────────────────────┐
│         Ingress (dockey.example.com)         │
│  ┌────────────────────────────────────────┐  │
│  │ /           → frontend:80              │  │
│  │ /api/users  → user-service:8081        │  │
│  │ /api/docs   → docs-service:8080        │  │
│  │ /api/comments → comments-service:8082  │  │
│  │ /auth       → keycloak:8080            │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```

## Build Modes

The Dockerfile supports different environment configurations via `BUILD_ENV` argument:

### 1. **Local Development (Vite Proxy)**
```bash
npm run dev
```
- Uses Vite proxy to forward API requests to localhost:8080/8081/8082
- No need to build Docker image
- Hot reload enabled

### 2. **Docker Compose**
```bash
docker build -t dockey-frontend .
docker run -p 3000:80 dockey-frontend
```
- Frontend accesses services at `http://localhost:8081`, etc.
- Uses default `.env` or `.env.production`

### 3. **Kubernetes (Helm)**
```bash
docker build --build-arg BUILD_ENV=k8s -t dockey.azurecr.io/frontend:latest .
docker push dockey.azurecr.io/frontend:latest
helm upgrade --install dockey ./helm/dockey -n dockey
```
- Frontend uses `/api/users`, `/api/docs`, `/api/comments` paths
- Ingress rewrites paths and routes to backend services
- Uses `.env.k8s` configuration

## Environment Variables

### `.env` (Local Development)
```env
# Keep commented for Vite proxy mode
# VITE_USER_SERVICE_URL=
# VITE_DOCS_SERVICE_URL=
# VITE_COMMENTS_SERVICE_URL=
```

### `.env.k8s` (Kubernetes)
```env
VITE_USER_SERVICE_URL=/api/users
VITE_DOCS_SERVICE_URL=/api/docs
VITE_COMMENTS_SERVICE_URL=/api/comments
```

These are baked into the build at compile time (Vite replaces `import.meta.env.VITE_*` with literal strings).

## Deployment Steps

### 1. Build Image for K8s
```bash
cd app/frontend

# Build with K8s environment
docker build --build-arg BUILD_ENV=k8s -t dockey.azurecr.io/frontend:latest .

# Push to ACR
docker push dockey.azurecr.io/frontend:latest
```

### 2. Deploy with Helm
```bash
# Install or upgrade
helm upgrade --install dockey ./helm/dockey -n dockey

# Check deployment
kubectl get pods -n dockey -l app=frontend
kubectl get svc -n dockey frontend
```

### 3. Configure Ingress DNS
Point your domain to the Ingress controller's external IP:
```bash
kubectl get ingress -n dockey dockey-ingress

# Example output:
# NAME              CLASS   HOSTS                   ADDRESS         PORTS
# dockey-ingress    nginx   dockey.example.com      20.30.40.50     80, 443
```

Update DNS: `dockey.example.com` → `20.30.40.50`

## Helm Configuration

The frontend is configured in `helm/dockey/values.yaml`:

```yaml
services:
  frontend:
    enabled: true
    image:
      repository: frontend
      tag: latest
    replicas: 2  # Horizontally scalable (Nginx is stateless)
    port: 80
    resources:
      requests:
        memory: "32Mi"
        cpu: "10m"
      limits:
        memory: "64Mi"
        cpu: "100m"
```

## Scaling

Scale frontend pods:
```bash
kubectl scale deployment frontend -n dockey --replicas=3

# Or via Helm
helm upgrade --install dockey ./helm/dockey -n dockey \
  --set services.frontend.replicas=3
```

## Troubleshooting

### Check frontend logs
```bash
kubectl logs -n dockey -l app=frontend --tail=50
```

### Test frontend health
```bash
kubectl exec -n dockey deployment/frontend -- curl http://localhost:80/health
```

### Check Ingress routing
```bash
curl -H "Host: dockey.example.com" http://<INGRESS_IP>/
curl -H "Host: dockey.example.com" http://<INGRESS_IP>/api/users/health
```

### API calls return 404
- Verify Ingress rewrite rules are correct
- Check backend services are running: `kubectl get pods -n dockey`
- Verify service names match Ingress backend references

### CORS errors
- Backend services need CORS filters (already added to all three services)
- Rebuild backend images if CORS filters were added recently

## Production Checklist

- [ ] Update `dockey.example.com` to your actual domain
- [ ] Configure cert-manager for TLS certificates
- [ ] Set up CDN (optional) for static assets
- [ ] Enable Horizontal Pod Autoscaler (HPA) for frontend
- [ ] Configure resource limits based on traffic
- [ ] Set up monitoring with Prometheus metrics
- [ ] Configure log aggregation (ELK/Loki)

## Related Files

- `helm/dockey/templates/services/frontend.yaml` - K8s Deployment & Service
- `helm/dockey/values.yaml` - Configuration values
- `k8s/ingress/ingress.yaml` - Ingress routing rules
- `app/frontend/.env.k8s` - K8s environment variables
- `app/frontend/Dockerfile` - Multi-stage build with BUILD_ENV support
