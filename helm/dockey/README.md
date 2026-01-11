# Dockey Helm Chart

This Helm chart deploys the Dockey microservices application to Kubernetes.

## Prerequisites

- Kubernetes 1.19+
- Helm 3.0+
- kubectl configured to access your cluster
- Secrets created (see below)

## Installation

### 1. Create Secrets

Before installing the chart, create the required secrets:

```bash
# Set your passwords
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

### 2. Install the Chart

```bash
# Install with default values
helm install dockey ./helm/dockey -n dockey

# Install with free tier optimized values (for limited resources)
helm install dockey ./helm/dockey -n dockey -f ./helm/dockey/values-free-tier.yaml

# Install with custom values file
helm install dockey ./helm/dockey -n dockey -f my-values.yaml

# Install with specific image tags
helm install dockey ./helm/dockey -n dockey \
  --set services.comments.image.tag=v1.0.0 \
  --set services.docs.image.tag=v1.0.0 \
  --set services.user.image.tag=v1.0.0
```

### 3. Upgrade the Chart

```bash
# Upgrade with new values
helm upgrade dockey ./helm/dockey -n dockey

# Upgrade with new image tags
helm upgrade dockey ./helm/dockey -n dockey \
  --set services.comments.image.tag=v1.1.0 \
  --set services.docs.image.tag=v1.1.0 \
  --set services.user.image.tag=v1.1.0

# Upgrade and wait for rollout
helm upgrade dockey ./helm/dockey -n dockey --wait
```

### 4. Uninstall the Chart

```bash
helm uninstall dockey -n dockey
```

## Configuration

The following table lists the configurable parameters and their default values:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `global.namespace` | Namespace for all resources | `dockey` |
| `global.imageRegistry` | Container registry | `dockey.azurecr.io` |
| `global.imagePullPolicy` | Image pull policy | `Always` |
| `global.storageClass` | Storage class for PVCs | `managed-csi` |
| `services.comments.enabled` | Enable comments service | `true` |
| `services.comments.replicas` | Number of replicas | `2` |
| `services.comments.image.tag` | Image tag | `latest` |
| `services.docs.enabled` | Enable docs service | `true` |
| `services.docs.replicas` | Number of replicas | `2` |
| `services.docs.image.tag` | Image tag | `latest` |
| `services.user.enabled` | Enable user service | `true` |
| `services.user.replicas` | Number of replicas | `2` |
| `services.user.image.tag` | Image tag | `latest` |
| `databases.postgres.*.enabled` | Enable PostgreSQL databases | `true` |
| `databases.mongodb.comments.enabled` | Enable MongoDB | `true` |
| `messaging.zookeeper.enabled` | Enable Zookeeper | `true` |
| `messaging.kafka.enabled` | Enable Kafka | `true` |
| `auth.keycloak.enabled` | Enable Keycloak | `true` |
| `monitoring.prometheus.enabled` | Enable Prometheus | `true` |
| `ingress.enabled` | Enable Ingress | `false` |

See `values.yaml` for all available options.

## Examples

### Production Deployment

```bash
helm install dockey ./helm/dockey -n dockey \
  --set services.comments.replicas=3 \
  --set services.docs.replicas=3 \
  --set services.user.replicas=3 \
  --set services.comments.image.tag=v1.0.0 \
  --set services.docs.image.tag=v1.0.0 \
  --set services.user.image.tag=v1.0.0 \
  --set ingress.enabled=true
```

### Development Deployment

```bash
helm install dockey ./helm/dockey -n dockey \
  --set services.comments.replicas=1 \
  --set services.docs.replicas=1 \
  --set services.user.replicas=1 \
  --set monitoring.prometheus.enabled=false
```

### Custom Values File

Create `production-values.yaml`:

```yaml
global:
  imageRegistry: dockey.azurecr.io
  storageClass: managed-csi

services:
  comments:
    replicas: 3
    image:
      tag: v1.0.0
  docs:
    replicas: 3
    image:
      tag: v1.0.0
  user:
    replicas: 3
    image:
      tag: v1.0.0

ingress:
  enabled: true
  hosts:
    - host: api.dockey.example.com
      paths:
        - path: /comments
          service: comments-service
          port: 8082
```

Then install:

```bash
helm install dockey ./helm/dockey -n dockey -f production-values.yaml
```

## Troubleshooting

### Check Release Status

```bash
helm status dockey -n dockey
```

### View Rendered Templates

```bash
helm template dockey ./helm/dockey -n dockey
```

### Debug Installation

```bash
helm install dockey ./helm/dockey -n dockey --debug --dry-run
```

### View Release History

```bash
helm history dockey -n dockey
```

### Rollback

```bash
helm rollback dockey <revision-number> -n dockey
```

## Notes

- Secrets must be created before installation
- The chart creates a namespace if it doesn't exist
- All resources are deployed to the namespace specified in `global.namespace`
- Image tags should be updated for each deployment
- Use `--wait` flag to wait for all resources to be ready
