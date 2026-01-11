# Kubernetes Secrets

**⚠️ IMPORTANT: Never commit actual secrets to this repository!**

This directory contains templates for Kubernetes secrets. Use one of the following methods to create secrets:

## Method 1: Using kubectl (Quick Setup)

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

# MongoDB secret (replace <strong-password> with actual password)
kubectl create secret generic mongodb-comments-secret \
  --from-literal=username=admin \
  --from-literal=password=<strong-password> \
  --from-literal=connection-string=mongodb://admin:<strong-password>@mongodb-comments:27017/commentsdb?authSource=admin \
  --namespace dockey

# Keycloak admin secret
kubectl create secret generic keycloak-admin-secret \
  --from-literal=username=admin \
  --from-literal=password=<strong-password> \
  --namespace dockey
```

## Method 2: Using Sealed Secrets (Recommended for GitOps)

Install Sealed Secrets controller and use sealed-secrets.yaml files.

## Method 3: Using Azure Key Vault (Recommended for Production)

Use Azure Key Vault CSI driver to mount secrets directly from Azure Key Vault.

See DEPLOYMENT.md for detailed instructions.

## Secret Structure

Each secret should contain:
- `username`: Database/service username
- `password`: Strong password (minimum 16 characters)
- `connection-string`: (For MongoDB) Full connection string
