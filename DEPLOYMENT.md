# Dockey - Azure Kubernetes Service (AKS) Deployment Guide

This guide provides step-by-step instructions for deploying the Dockey microservices application to Azure Kubernetes Service (AKS) using a CI/CD pipeline.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Azure Resource Setup](#azure-resource-setup)
3. [Local Setup](#local-setup)
4. [Kubernetes Manifests](#kubernetes-manifests)
5. [CI/CD Pipeline Setup](#cicd-pipeline-setup)
6. [Secrets Management](#secrets-management)
7. [Deployment Steps](#deployment-steps)
8. [Post-Deployment Verification](#post-deployment-verification)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Tools

- **Azure CLI** (v2.50.0 or later)
- **Helm** (v3.0.0 or later) - **Required for deployment**
- **kubectl** (v1.32.7 or later, matching AKS version)
- **Docker** (for local testing)
- **Git** (for repository access)
- **Maven** (for local builds, optional)

### Azure Account Requirements

- Active Azure subscription
- Contributor or Owner role on the subscription/resource group
- Access to Azure Container Registry (ACR): `dockey.azurecr.io`
- Access to AKS cluster: `dockey1` in resource group `FRItest`

### CI/CD Platform

Choose one:
- **GitHub Actions** (recommended for GitHub repositories)
- **Azure DevOps Pipelines** (recommended for Azure DevOps repositories)

---

## Azure Resource Setup

### 1. Verify ACR Access

```bash
# Login to Azure
az login

# Set your subscription
az account set --subscription 8ada3dd2-9913-4ecd-9440-344b8563e22b

# Verify ACR access
az acr login --name dockey

# Build and push images to ACR (see below)
# After pushing images, list repositories (should show your services)
az acr repository list --name dockey --output table
```

### 1.1. Build and Push Images to ACR

Before you can deploy to AKS, you need to build and push your Docker images to ACR. You can do this in two ways:

#### Option 1: Using the Automated Script (Recommended)

```bash
# From the repository root
./scripts/build-and-push-to-acr.sh [tag]

# Example with specific tag
./scripts/build-and-push-to-acr.sh v1.0.0

# Example with default 'latest' tag
./scripts/build-and-push-to-acr.sh
```

#### Option 2: Manual Build and Push

```bash
# Login to ACR
az acr login --name dockey

# Build and push comments-service
cd app/comments-service
docker build -t dockey.azurecr.io/comments-service:latest .
docker push dockey.azurecr.io/comments-service:latest
cd ../..

# Build and push docs-service
cd app/docs-service
docker build -t dockey.azurecr.io/docs-service:latest .
docker push dockey.azurecr.io/docs-service:latest
cd ../..

# Build and push user-service
cd app/user-service
docker build -t dockey.azurecr.io/user-service:latest .
docker push dockey.azurecr.io/user-service:latest
cd ../..
```

#### Verify Images in ACR

```bash
# List all repositories
az acr repository list --name dockey --output table

# List tags for a specific repository
az acr repository show-tags --name dockey --repository comments-service --output table
az acr repository show-tags --name dockey --repository docs-service --output table
az acr repository show-tags --name dockey --repository user-service --output table
```

### 2. Configure AKS Credentials

```bash
# Get AKS credentials
az aks get-credentials \
  --resource-group FRItest \
  --name dockey1 \
  --overwrite-existing

# Verify connection
kubectl get nodes
```

### 3. Attach ACR to AKS (if not already done)

**Important**: AKS needs permission to pull images from ACR. If you see "401 Unauthorized" errors, run this:

#### Option 1: Using the setup script (Recommended)

```bash
./scripts/setup-acr-access.sh
```

#### Option 2: Manual setup

```bash
# Get AKS managed identity (SystemAssigned)
AKS_ID=$(az aks show \
  --resource-group FRItest \
  --name dockey1 \
  --query identity.principalId \
  --output tsv)

# Get ACR resource ID
ACR_ID=$(az acr show \
  --name dockey \
  --query id \
  --output tsv)

# Grant AKS pull permissions to ACR
az role assignment create \
  --assignee $AKS_ID \
  --role AcrPull \
  --scope $ACR_ID

# Wait for propagation (10-30 seconds)
sleep 10

# Restart deployments to pick up new permissions
kubectl rollout restart deployment -n dockey
```

### 4. Create Resource Group (if needed)

```bash
az group create \
  --name FRItest \
  --location switzerlandnorth
```

---

## Local Setup

### 1. Clone Repository

```bash
git clone <your-repository-url>
cd dockey
```

### 2. Install kubectl (if not installed)

```bash
# macOS
brew install kubectl

# Linux
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Verify
kubectl version --client
```

### 3. Verify Docker Builds Locally

```bash
# Test building images
cd app/comments-service
docker build -t dockey/comments-service:test .
cd ../docs-service
docker build -t dockey/docs-service:test .
cd ../user-service
docker build -t dockey/user-service:test .
```

---

## Helm Chart

The deployment uses **Helm** for managing Kubernetes resources. The Helm chart is located in the `helm/dockey/` directory.

### Chart Structure

```
helm/dockey/
├── Chart.yaml              # Chart metadata
├── values.yaml             # Default configuration values
├── templates/              # Kubernetes manifest templates
│   ├── _helpers.tpl        # Template helpers
│   ├── namespace.yaml
│   ├── services/
│   │   ├── comments-service.yaml
│   │   ├── docs-service.yaml
│   │   └── user-service.yaml
│   ├── databases/
│   │   ├── postgres-docs.yaml
│   │   ├── postgres-users.yaml
│   │   ├── postgres-keycloak.yaml
│   │   └── mongodb-comments.yaml
│   ├── messaging/
│   │   ├── zookeeper.yaml
│   │   └── kafka.yaml
│   ├── auth/
│   │   └── keycloak.yaml
│   ├── monitoring/
│   │   └── prometheus.yaml
│   └── ingress.yaml
└── README.md               # Helm chart documentation
```

### Installing Helm

```bash
# macOS
brew install helm

# Linux
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Verify installation
helm version
```

### Key Configuration Points

1. **Service URLs**: Configured via values.yaml
2. **Database Connections**: Use Kubernetes service names (e.g., `postgres-docs:5432`)
3. **Kafka Bootstrap**: Use `kafka:9092` for internal communication
4. **Keycloak URL**: Use `http://keycloak:8080` for internal services
5. **Image Tags**: Set via Helm values or command-line flags

For detailed Helm chart documentation, see [helm/dockey/README.md](./helm/dockey/README.md).

---

## CI/CD Pipeline Setup

### Option 1: GitHub Actions

Create `.github/workflows/deploy-aks.yml` (see provided template).

**Required GitHub Secrets:**
- `AZURE_CLIENT_ID`: Service principal client ID
- `AZURE_CLIENT_SECRET`: Service principal client secret
- `AZURE_TENANT_ID`: Azure tenant ID
- `AZURE_SUBSCRIPTION_ID`: `8ada3dd2-9913-4ecd-9440-344b8563e22b`
- `ACR_NAME`: `dockey`
- `AKS_RESOURCE_GROUP`: `FRItest`
- `AKS_CLUSTER_NAME`: `dockey1`

**Create Service Principal:**

```bash
# Create service principal for GitHub Actions
az ad sp create-for-rbac \
  --name "dockey-github-actions" \
  --role contributor \
  --scopes /subscriptions/8ada3dd2-9913-4ecd-9440-344b8563e22b/resourceGroups/FRItest \
  --sdk-auth

# Grant ACR push permissions
ACR_ID=$(az acr show --name dockey --resource-group FRItest --query id --output tsv)
SP_ID=$(az ad sp show --id http://dockey-github-actions --query appId --output tsv)
az role assignment create --assignee $SP_ID --role AcrPush --scope $ACR_ID
```

### Option 2: Azure DevOps

Create `azure-pipelines.yml` (see provided template).

**Required Azure DevOps Variables:**
- `ACR_NAME`: `dockey`
- `AKS_RESOURCE_GROUP`: `FRItest`
- `AKS_CLUSTER_NAME`: `dockey1`
- `IMAGE_TAG`: `$(Build.BuildId)` or `$(Build.SourceVersion)`

**Service Connection:**
Create an Azure Resource Manager service connection in Azure DevOps pointing to your subscription.

---

## Secrets Management

### Create Kubernetes Secrets

**Important**: Never commit secrets to the repository. Use one of these methods:

#### Method 1: kubectl (for initial setup)

```bash
# PostgreSQL secrets
kubectl create secret generic postgres-docs-secret \
  --from-literal=username=postgres \
  --from-literal=password=<strong-password> \
  --namespace dockey

kubectl create secret generic postgres-users-secret \
  --from-literal=username=postgres \
  --from-literal=password=<strong-password> \
  --namespace dockey

kubectl create secret generic postgres-keycloak-secret \
  --from-literal=username=keycloak \
  --from-literal=password=<strong-password> \
  --namespace dockey

# MongoDB secrets
kubectl create secret generic mongodb-comments-secret \
  --from-literal=username=admin \
  --from-literal=password=<strong-password> \
  --namespace dockey

# Keycloak admin
kubectl create secret generic keycloak-admin-secret \
  --from-literal=username=admin \
  --from-literal=password=<strong-password> \
  --namespace dockey
```

#### Method 2: Azure Key Vault (Recommended for Production)

```bash
# Create Key Vault
az keyvault create \
  --name dockey-keyvault \
  --resource-group FRItest \
  --location switzerlandnorth

# Store secrets
az keyvault secret set --vault-name dockey-keyvault --name postgres-docs-password --value <password>
az keyvault secret set --vault-name dockey-keyvault --name postgres-users-password --value <password>
az keyvault secret set --vault-name dockey-keyvault --name postgres-keycloak-password --value <password>
az keyvault secret set --vault-name dockey-keyvault --name mongodb-password --value <password>
az keyvault secret set --vault-name dockey-keyvault --name keycloak-admin-password --value <password>

# Install Secrets Store CSI Driver
kubectl apply -f https://raw.githubusercontent.com/Azure/secrets-store-csi-driver-provider-azure/master/deployment/crd-secrets-store-csi-driver.yaml
kubectl apply -f https://raw.githubusercontent.com/Azure/secrets-store-csi-driver-provider-azure/master/deployment/rbac-secrets-store-csi-driver.yaml
kubectl apply -f https://raw.githubusercontent.com/Azure/secrets-store-csi-driver-provider-azure/master/deployment/secrets-store-csi-driver.yaml
```

---

## Deployment Steps

### 1. Initial Deployment (Manual)

#### Option A: Using Helm (Recommended)

```bash
# Create secrets (see Secrets Management section)
# ... create secrets ...

# Install using Helm script
./scripts/deploy-helm.sh [image-tag]

# Or install manually with Helm
helm install dockey ./helm/dockey -n dockey \
  --set services.comments.image.tag=latest \
  --set services.docs.image.tag=latest \
  --set services.user.image.tag=latest \
  --wait \
  --timeout 10m
```

#### Option B: Using kubectl (Legacy)

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Create secrets (see Secrets Management section)
# ... create secrets ...

# Deploy databases
kubectl apply -f k8s/databases/

# Wait for databases to be ready
kubectl wait --for=condition=ready pod -l app=postgres-docs -n dockey --timeout=300s
kubectl wait --for=condition=ready pod -l app=postgres-users -n dockey --timeout=300s
kubectl wait --for=condition=ready pod -l app=postgres-keycloak -n dockey --timeout=300s
kubectl wait --for=condition=ready pod -l app=mongodb-comments -n dockey --timeout=300s

# Deploy messaging infrastructure
kubectl apply -f k8s/messaging/

# Wait for Kafka to be ready
kubectl wait --for=condition=ready pod -l app=kafka -n dockey --timeout=300s

# Deploy Keycloak
kubectl apply -f k8s/auth/keycloak.yaml

# Wait for Keycloak
kubectl wait --for=condition=ready pod -l app=keycloak -n dockey --timeout=600s

# Deploy application services
kubectl apply -f k8s/services/

# Deploy monitoring
kubectl apply -f k8s/monitoring/

# Deploy ingress (if using)
kubectl apply -f k8s/ingress/
```

### 2. Automated Deployment via CI/CD

Once the CI/CD pipeline is configured:

1. **Push code to main/master branch** → Triggers build and deployment
2. **Create a release tag** → Triggers production deployment
3. **Monitor pipeline** → Check GitHub Actions or Azure DevOps for status

### 3. Rolling Updates

The CI/CD pipeline automatically performs rolling updates using Helm:

```bash
# Manual rolling update with Helm (recommended)
helm upgrade dockey ./helm/dockey -n dockey \
  --set services.comments.image.tag=new-tag \
  --set services.docs.image.tag=new-tag \
  --set services.user.image.tag=new-tag \
  --wait \
  --timeout 10m

# Or using kubectl (legacy)
kubectl set image deployment/comments-service \
  comments-service=dockey.azurecr.io/comments-service:new-tag \
  -n dockey

kubectl rollout status deployment/comments-service -n dockey
```

---

## Post-Deployment Verification

### 1. Check Pod Status

```bash
# Check all pods
kubectl get pods -n dockey

# Check specific service
kubectl get pods -l app=comments-service -n dockey
kubectl get pods -l app=docs-service -n dockey
kubectl get pods -l app=user-service -n dockey
```

### 2. Check Service Endpoints

```bash
# List services
kubectl get svc -n dockey

# Port forward for testing
kubectl port-forward svc/comments-service 8082:8082 -n dockey
kubectl port-forward svc/docs-service 8080:8080 -n dockey
kubectl port-forward svc/user-service 8081:8081 -n dockey
```

### 3. Health Checks

```bash
# Test health endpoints
curl http://localhost:8082/health  # comments-service
curl http://localhost:8080/health  # docs-service
curl http://localhost:8081/health  # user-service
```

### 4. Check Logs

```bash
# View logs
kubectl logs -l app=comments-service -n dockey --tail=100
kubectl logs -l app=docs-service -n dockey --tail=100
kubectl logs -l app=user-service -n dockey --tail=100
```

### 5. Verify Database Connections

```bash
# Check database pods
kubectl exec -it <postgres-docs-pod> -n dockey -- psql -U postgres -d docsdb -c "\dt"
kubectl exec -it <postgres-users-pod> -n dockey -- psql -U postgres -d usersdb -c "\dt"
kubectl exec -it <mongodb-pod> -n dockey -- mongosh -u admin -p <password> --eval "show dbs"
```

---

## Troubleshooting

### Common Issues

#### 1. Image Pull Errors

```bash
# Verify ACR authentication
az acr login --name dockey

# Check image pull secrets
kubectl get secrets -n dockey | grep acr

# Verify AKS can pull from ACR
kubectl describe pod <pod-name> -n dockey | grep -A 5 Events
```

#### 2. Database Connection Failures

```bash
# Check database pods
kubectl get pods -l app=postgres-docs -n dockey

# Check database logs
kubectl logs <postgres-pod> -n dockey

# Verify service endpoints
kubectl get svc -n dockey | grep postgres

# Test connection from a pod
kubectl run -it --rm debug --image=postgres:15-alpine --restart=Never -n dockey -- \
  psql -h postgres-docs -U postgres -d docsdb
```

#### 3. Kafka Connection Issues

```bash
# Check Kafka pods
kubectl get pods -l app=kafka -n dockey

# Check Kafka logs
kubectl logs <kafka-pod> -n dockey

# Verify Kafka service
kubectl get svc kafka -n dockey

# Test from application pod
kubectl exec -it <comments-service-pod> -n dockey -- \
  sh -c "echo 'test' | kafka-console-producer --bootstrap-server kafka:9092 --topic test"
```

#### 4. Keycloak Connection Issues

```bash
# Check Keycloak pod
kubectl get pods -l app=keycloak -n dockey

# Check Keycloak logs
kubectl logs <keycloak-pod> -n dockey --tail=100

# Verify Keycloak is ready
kubectl exec -it <keycloak-pod> -n dockey -- \
  curl http://localhost:8080/health/ready
```

#### 5. Out of Memory/CPU Issues

```bash
# Check resource usage
kubectl top pods -n dockey

# Check node resources
kubectl top nodes

# Scale up if needed
kubectl scale deployment comments-service --replicas=3 -n dockey
```

#### 6. Service Not Accessible

```bash
# Check ingress
kubectl get ingress -n dockey

# Check service endpoints
kubectl get endpoints -n dockey

# Check for network policies blocking traffic
kubectl get networkpolicies -n dockey
```

### Debugging Commands

```bash
# Describe pod for events
kubectl describe pod <pod-name> -n dockey

# Get events
kubectl get events -n dockey --sort-by='.lastTimestamp'

# Check config maps
kubectl get configmaps -n dockey
kubectl describe configmap <configmap-name> -n dockey

# Check secrets (values are base64 encoded)
kubectl get secrets -n dockey
kubectl get secret <secret-name> -n dockey -o yaml
```

---

## Environment Variables Reference

### Comments Service
- `MONGODB_CONNECTION_STRING`: MongoDB connection string
- `MONGODB_DATABASE`: Database name (default: `commentsdb`)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: `kafka:9092`)
- `KEYCLOAK_AUTHSERVERURL`: Keycloak server URL
- `KEYCLOAK_REALM`: Keycloak realm (default: `dockey`)

### Docs Service
- `KUMULUZEE_DATASOURCES0_CONNECTIONURL`: PostgreSQL connection URL
- `KUMULUZEE_DATASOURCES0_USERNAME`: Database username
- `KUMULUZEE_DATASOURCES0_PASSWORD`: Database password
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers
- `KAFKA_CONSUMER_GROUP_ID`: Consumer group ID

### User Service
- `KUMULUZEE_DATASOURCES0_CONNECTIONURL`: PostgreSQL connection URL
- `KUMULUZEE_DATASOURCES0_USERNAME`: Database username
- `KUMULUZEE_DATASOURCES0_PASSWORD`: Database password
- `KEYCLOAK_AUTHSERVERURL`: Keycloak server URL
- `KEYCLOAK_REALM`: Keycloak realm
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers

---

## Scaling

### Horizontal Pod Autoscaling

```bash
# Create HPA for services
kubectl autoscale deployment comments-service \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n dockey

kubectl autoscale deployment docs-service \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n dockey

kubectl autoscale deployment user-service \
  --cpu-percent=70 \
  --min=2 \
  --max=10 \
  -n dockey
```

### Manual Scaling

```bash
kubectl scale deployment comments-service --replicas=3 -n dockey
kubectl scale deployment docs-service --replicas=3 -n dockey
kubectl scale deployment user-service --replicas=3 -n dockey
```

---

## Backup and Recovery

### Database Backups

```bash
# PostgreSQL backup
kubectl exec -it <postgres-pod> -n dockey -- \
  pg_dump -U postgres docsdb > docsdb-backup.sql

# MongoDB backup
kubectl exec -it <mongodb-pod> -n dockey -- \
  mongodump --uri="mongodb://admin:<password>@localhost:27017/commentsdb" --out=/tmp/backup
```

### Restore

```bash
# PostgreSQL restore
kubectl exec -i <postgres-pod> -n dockey -- \
  psql -U postgres docsdb < docsdb-backup.sql

# MongoDB restore
kubectl exec -it <mongodb-pod> -n dockey -- \
  mongorestore --uri="mongodb://admin:<password>@localhost:27017" /tmp/backup
```

---

## Monitoring and Logging

### Prometheus

Access Prometheus UI:
```bash
kubectl port-forward svc/prometheus 9090:9090 -n dockey
# Open http://localhost:9090
```

### Application Logs

```bash
# Stream logs
kubectl logs -f -l app=comments-service -n dockey

# Logs from all services
kubectl logs -f -l app=comments-service -n dockey &
kubectl logs -f -l app=docs-service -n dockey &
kubectl logs -f -l app=user-service -n dockey &
```

---

## Security Best Practices

1. **Use Azure Key Vault** for secrets management
2. **Enable network policies** to restrict pod-to-pod communication
3. **Use RBAC** to limit access to Kubernetes resources
4. **Scan images** for vulnerabilities using ACR security features
5. **Use HTTPS** for all external traffic (configure TLS in ingress)
6. **Regular updates** of base images and dependencies
7. **Monitor** for security vulnerabilities using Azure Security Center

---

## Cost Optimization

1. **Use Azure Spot VMs** for non-critical workloads
2. **Right-size** your node pools based on actual usage
3. **Enable cluster autoscaler** (already configured)
4. **Use managed databases** (Azure Database for PostgreSQL, Cosmos DB) for production
5. **Monitor costs** using Azure Cost Management

---

## Support and Resources

- **Azure AKS Documentation**: https://docs.microsoft.com/azure/aks/
- **Kubernetes Documentation**: https://kubernetes.io/docs/
- **Azure Container Registry**: https://docs.microsoft.com/azure/container-registry/
- **KumuluzEE Documentation**: https://www.kumuluz.com/

---

## Next Steps

1. Set up monitoring and alerting (Azure Monitor, Grafana)
2. Configure CI/CD pipeline for automated deployments
3. Set up staging environment for testing
4. Implement blue-green or canary deployments
5. Configure backup automation
6. Set up log aggregation (Azure Log Analytics)

---

**Last Updated**: 2026-01-11
**Maintained By**: DevOps Team
