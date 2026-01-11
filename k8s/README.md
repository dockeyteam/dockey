# Kubernetes Manifests

This directory contains all Kubernetes manifests for deploying the Dockey application to AKS.

## Directory Structure

```
k8s/
├── namespace.yaml              # Namespace definition
├── secrets/                    # Secret templates (DO NOT commit actual secrets)
│   └── README.md
├── databases/                  # Database deployments
│   ├── postgres-docs.yaml
│   ├── postgres-users.yaml
│   ├── postgres-keycloak.yaml
│   └── mongodb-comments.yaml
├── messaging/                  # Kafka and Zookeeper
│   ├── zookeeper.yaml
│   └── kafka.yaml
├── auth/                       # Keycloak
│   └── keycloak.yaml
├── services/                   # Application services
│   ├── comments-service.yaml
│   ├── docs-service.yaml
│   └── user-service.yaml
├── monitoring/                 # Prometheus
│   └── prometheus.yaml
└── ingress/                     # Ingress configuration
    └── ingress.yaml
```

## Deployment Order

Deploy resources in this order:

1. **Namespace**: `kubectl apply -f namespace.yaml`
2. **Secrets**: Create secrets (see secrets/README.md)
3. **Databases**: `kubectl apply -f databases/`
4. **Messaging**: `kubectl apply -f messaging/`
5. **Auth**: `kubectl apply -f auth/`
6. **Services**: `kubectl apply -f services/`
7. **Monitoring**: `kubectl apply -f monitoring/`
8. **Ingress**: `kubectl apply -f ingress/` (optional)

## Quick Deploy

```bash
# Apply all manifests (after creating secrets)
kubectl apply -f namespace.yaml
kubectl apply -f databases/
kubectl apply -f messaging/
kubectl apply -f auth/
kubectl apply -f services/
kubectl apply -f monitoring/
kubectl apply -f ingress/
```

## Customization

### Image Tags

Update image tags in service manifests:
- `comments-service.yaml`
- `docs-service.yaml`
- `user-service.yaml`

### Resource Limits

Adjust CPU and memory requests/limits based on your cluster capacity and workload requirements.

### Storage

All StatefulSets use `managed-csi` storage class. Adjust `storageClassName` if using different storage provisioner.

### Replicas

Default replicas are set to 2 for services and 1 for databases. Adjust based on your HA requirements.

## Notes

- All resources are deployed to the `dockey` namespace
- Services use ClusterIP (internal only)
- Use Ingress for external access
- Secrets must be created before deploying services that use them
- Database StatefulSets require persistent storage
