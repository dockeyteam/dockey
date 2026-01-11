# Quick Start Guide - AKS Deployment

This is a condensed quick reference guide. For detailed instructions, see [DEPLOYMENT.md](./DEPLOYMENT.md).

## Prerequisites Check

```bash
# Verify Azure CLI
az --version

# Verify kubectl
kubectl version --client

# Verify Docker
docker --version

# Login to Azure
az login

# Set subscription
az account set --subscription 8ada3dd2-9913-4ecd-9440-344b8563e22b

# Get AKS credentials
az aks get-credentials --resource-group FRItest --name dockey1 --overwrite-existing

# Verify connection
kubectl get nodes

# Setup ACR access for AKS (IMPORTANT - prevents 401 Unauthorized errors)
./scripts/setup-acr-access.sh
```

## Build and Push Images to ACR

**Important**: You must build and push images to ACR before deploying to AKS.

```bash
# Option 1: Use the automated script (recommended)
./scripts/build-and-push-to-acr.sh

# Option 2: Manual build and push
az acr login --name dockey

# Build and push each service
docker build -t dockey.azurecr.io/comments-service:latest ./app/comments-service
docker push dockey.azurecr.io/comments-service:latest

docker build -t dockey.azurecr.io/docs-service:latest ./app/docs-service
docker push dockey.azurecr.io/docs-service:latest

docker build -t dockey.azurecr.io/user-service:latest ./app/user-service
docker push dockey.azurecr.io/user-service:latest

# Verify images
az acr repository list --name dockey --output table
```

## Create Secrets

```bash
# Set your passwords (use strong passwords!)
export POSTGRES_PASSWORD="your-strong-password"
export MONGODB_PASSWORD="your-strong-password"
export KEYCLOAK_ADMIN_PASSWORD="your-strong-password"

# Create namespace
kubectl create namespace dockey

# PostgreSQL secrets
kubectl create secret generic postgres-docs-secret \
  --from-literal=username=postgres \
  --from-literal=password=$POSTGRES_PASSWORD \
  -n dockey

kubectl create secret generic postgres-users-secret \
  --from-literal=username=postgres \
  --from-literal=password=$POSTGRES_PASSWORD \
  -n dockey

kubectl create secret generic postgres-keycloak-secret \
  --from-literal=username=keycloak \
  --from-literal=password=$POSTGRES_PASSWORD \
  -n dockey

# MongoDB secret
kubectl create secret generic mongodb-comments-secret \
  --from-literal=username=admin \
  --from-literal=password=$MONGODB_PASSWORD \
  --from-literal=connection-string="mongodb://admin:${MONGODB_PASSWORD}@mongodb-comments:27017/commentsdb?authSource=admin" \
  -n dockey

# Keycloak admin
kubectl create secret generic keycloak-admin-secret \
  --from-literal=username=admin \
  --from-literal=password=$KEYCLOAK_ADMIN_PASSWORD \
  -n dockey
```

## Install Helm

```bash
# macOS
brew install helm

# Linux
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Verify
helm version
```

## Deploy Application

### Option 1: Using the Helm deployment script (Recommended)

```bash
./scripts/deploy-helm.sh [image-tag]
```

### Option 2: Manual Helm deployment

```bash
# Install with default values
helm install dockey ./helm/dockey -n dockey --wait --timeout 15m

# Install with free tier optimized values (for limited resources)
helm install dockey ./helm/dockey -n dockey \
  -f ./helm/dockey/values-free-tier.yaml \
  --wait \
  --timeout 15m

# Install with specific image tags
helm install dockey ./helm/dockey -n dockey \
  --set services.comments.image.tag=latest \
  --set services.docs.image.tag=latest \
  --set services.user.image.tag=latest \
  --wait \
  --timeout 15m
```

### Option 3: Manual kubectl deployment (Legacy)

```bash
# 1. Namespace
kubectl apply -f k8s/namespace.yaml

# 2. Databases
kubectl apply -f k8s/databases/

# Wait for databases
kubectl wait --for=condition=ready pod -l app=postgres-docs -n dockey --timeout=300s
kubectl wait --for=condition=ready pod -l app=postgres-users -n dockey --timeout=300s
kubectl wait --for=condition=ready pod -l app=postgres-keycloak -n dockey --timeout=300s
kubectl wait --for=condition=ready pod -l app=mongodb-comments -n dockey --timeout=300s

# 3. Messaging
kubectl apply -f k8s/messaging/

# Wait for Kafka
kubectl wait --for=condition=ready pod -l app=kafka -n dockey --timeout=300s

# 4. Keycloak
kubectl apply -f k8s/auth/

# Wait for Keycloak
kubectl wait --for=condition=ready pod -l app=keycloak -n dockey --timeout=600s

# 5. Application services
kubectl apply -f k8s/services/

# 6. Monitoring
kubectl apply -f k8s/monitoring/

# 7. Ingress (optional)
kubectl apply -f k8s/ingress/
```

## Verify Deployment

```bash
# Check Helm release status
helm status dockey -n dockey

# Check all pods
kubectl get pods -n dockey

# Check services
kubectl get svc -n dockey

# Check deployments
kubectl get deployments -n dockey

# View logs
kubectl logs -f deployment/comments-service -n dockey
kubectl logs -f deployment/docs-service -n dockey
kubectl logs -f deployment/user-service -n dockey
```

## Access Services

```bash
# Port forward to access services locally
kubectl port-forward svc/comments-service 8082:8082 -n dockey
kubectl port-forward svc/docs-service 8080:8080 -n dockey
kubectl port-forward svc/user-service 8081:8081 -n dockey
kubectl port-forward svc/keycloak 8180:8080 -n dockey
kubectl port-forward svc/prometheus 9090:9090 -n dockey

# Test health endpoints
curl http://localhost:8082/health  # comments-service
curl http://localhost:8080/health  # docs-service
curl http://localhost:8081/health  # user-service
```

## CI/CD Setup

### GitHub Actions

1. Add secrets to GitHub repository:
   - `AZURE_CLIENT_ID`
   - `AZURE_CLIENT_SECRET`
   - `AZURE_TENANT_ID`
   - `AZURE_SUBSCRIPTION_ID`

2. Push to main/master branch to trigger deployment

### Azure DevOps

1. Create service connection to Azure subscription
2. Create variable group `dockey-variables`
3. Pipeline will run automatically on push to main/master

## Troubleshooting

```bash
# Describe pod for events
kubectl describe pod <pod-name> -n dockey

# Get events
kubectl get events -n dockey --sort-by='.lastTimestamp'

# Check resource usage
kubectl top pods -n dockey

# Restart a deployment
kubectl rollout restart deployment/<service-name> -n dockey

# Scale a service
kubectl scale deployment/<service-name> --replicas=3 -n dockey
```

## Upgrade Deployment

```bash
# Upgrade with new image tags
helm upgrade dockey ./helm/dockey -n dockey \
  --set services.comments.image.tag=new-tag \
  --set services.docs.image.tag=new-tag \
  --set services.user.image.tag=new-tag \
  --wait \
  --timeout 10m
```

## Cleanup

```bash
# Uninstall Helm release (recommended)
helm uninstall dockey -n dockey

# Delete namespace (removes all resources)
kubectl delete namespace dockey

# Clean up orphaned PersistentVolumes (if warnings appear)
# Sometimes PVs remain after namespace deletion
./scripts/cleanup-pvs.sh

# Or manually clean up orphaned PVs
kubectl get pv | grep Released
kubectl delete pv <pv-name>

# Or delete individually (legacy)
kubectl delete -f k8s/ingress/
kubectl delete -f k8s/monitoring/
kubectl delete -f k8s/services/
kubectl delete -f k8s/auth/
kubectl delete -f k8s/messaging/
kubectl delete -f k8s/databases/
kubectl delete -f k8s/namespace.yaml
```

## Complete Redeployment

If pods are stuck or not recovering, you can do a complete redeployment:

```bash
# Option 1: Use the redeploy script (recommended)
./scripts/redeploy.sh [image-tag] [values-file]

# Example with free-tier values
./scripts/redeploy.sh latest ./helm/dockey/values-free-tier.yaml

# Option 2: Manual redeployment
# Uninstall existing release
helm uninstall dockey -n dockey

# Wait for cleanup
sleep 30

# Reinstall
helm install dockey ./helm/dockey -n dockey --wait --timeout 15m
```

## Troubleshooting PersistentVolume Warnings

If you see warnings about `VolumeFailedDelete` after deleting a namespace:

1. **Check for orphaned PVs:**
   ```bash
   kubectl get pv | grep Released
   ```

2. **Clean up orphaned PVs:**
   ```bash
   ./scripts/cleanup-pvs.sh
   ```

3. **Or manually delete:**
   ```bash
   # List orphaned PVs
   kubectl get pv -o json | jq -r '.items[] | select(.status.phase == "Released") | .metadata.name'
   
   # Delete each one
   kubectl delete pv <pv-name>
   ```

These warnings are usually harmless and don't affect new deployments. The old PVs will eventually be cleaned up automatically, but you can force cleanup if needed.
