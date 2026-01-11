# Dockey Helm Chart

A comprehensive Helm chart for deploying the Dockey microservices platform to Kubernetes. This chart manages the complete application stack including microservices, databases, messaging infrastructure, authentication, and monitoring.

## Chart Structure

The chart is organized into logical components:

```
helm/dockey/
├── Chart.yaml              # Chart metadata
├── values.yaml             # Default configuration values
├── templates/
│   ├── _helpers.tpl        # Template helper functions
│   ├── services/           # Application microservices
│   │   ├── comments-service.yaml
│   │   ├── docs-service.yaml
│   │   └── user-service.yaml
│   ├── databases/          # Database StatefulSets
│   │   ├── postgres-docs.yaml
│   │   ├── postgres-users.yaml
│   │   ├── postgres-keycloak.yaml
│   │   └── mongodb-comments.yaml
│   ├── messaging/          # Kafka infrastructure
│   │   ├── zookeeper.yaml
│   │   └── kafka.yaml
│   ├── auth/               # Authentication service
│   │   └── keycloak.yaml
│   └── monitoring/         # Observability
│       └── prometheus.yaml
└── README.md
```

## Applications

### Microservices

The chart deploys three Java-based microservices built with KumuluzEE:

#### 1. Comments Service (`comments-service`)
- **Purpose**: Manages document comments and user interactions
- **Port**: 8082
- **Database**: MongoDB (`commentsdb`)
- **Dependencies**: Kafka, Keycloak
- **Features**: 
  - Comment CRUD operations
  - Like/unlike functionality
  - Line-level comment tracking

#### 2. Docs Service (`docs-service`)
- **Purpose**: Document management and processing
- **Port**: 8080
- **Database**: PostgreSQL (`docsdb`)
- **Dependencies**: Kafka (consumer)
- **Features**:
  - Document storage and retrieval
  - Document line comment aggregation
  - Kafka event consumption

#### 3. User Service (`user-service`)
- **Purpose**: User authentication and profile management
- **Port**: 8081
- **Database**: PostgreSQL (`usersdb`)
- **Dependencies**: Keycloak, Kafka
- **Features**:
  - User registration and authentication
  - Profile management
  - GraphQL API support
  - Token refresh

### Infrastructure Components

#### Databases
- **PostgreSQL**: Three separate instances for docs, users, and Keycloak
- **MongoDB**: Document store for comments service

#### Messaging
- **Zookeeper**: Coordination service for Kafka
- **Kafka**: Event streaming platform for inter-service communication

#### Authentication
- **Keycloak**: Identity and access management (IAM) service

#### Monitoring
- **Prometheus**: Metrics collection and alerting (optional)

## Configuration

### Global Settings

```yaml
global:
  namespace: dockey                    # Target Kubernetes namespace
  imageRegistry: dockey.azurecr.io      # Container registry
  imagePullPolicy: Always              # Image pull policy
  storageClass: managed-csi            # Storage class for PVCs
  javaOpts: "-XX:+UseSerialGC..."      # JVM options for all Java services
```

### Service Configuration

Each microservice supports the following configuration:

```yaml
services:
  <service-name>:
    enabled: true                       # Enable/disable service
    replicas: 1                         # Number of replicas
    image:
      repository: <service-name>        # Image repository
      tag: latest                       # Image tag
    port: 8080                          # Service port
    resources:
      requests:
        memory: "128Mi"
        cpu: "50m"
      limits:
        memory: "350Mi"
        cpu: "500m"
    env:                                # Environment variables
      databaseName: "docsdb"
      kafkaBootstrapServers: "kafka:9092"
    healthCheck:
      liveness:
        initialDelaySeconds: 120
        periodSeconds: 20
```


## Production Configuration

For production deployments, use the following recommended settings:


| Parameter | Description | Default |
|-----------|-------------|---------|
| `global.namespace` | Kubernetes namespace | `dockey` |
| `global.imageRegistry` | Container registry | `dockey.azurecr.io` |
| `global.imagePullPolicy` | Image pull policy | `Always` |
| `global.storageClass` | Storage class for PVCs | `managed-csi` |
| `global.javaOpts` | JVM options for Java services | Optimized for low memory |

### Service Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `services.<name>.enabled` | Enable service | `true` |
| `services.<name>.replicas` | Number of replicas | `1` |
| `services.<name>.image.repository` | Image repository | Service name |
| `services.<name>.image.tag` | Image tag | `latest` |
| `services.<name>.port` | Service port | Service-specific |
| `services.<name>.resources` | Resource requests/limits | See values.yaml |
| `services.<name>.env` | Environment variables | Service-specific |

### Database Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `databases.postgres.<name>.enabled` | Enable PostgreSQL instance | `true` |
| `databases.postgres.<name>.image.repository` | PostgreSQL image | `postgres` |
| `databases.postgres.<name>.image.tag` | PostgreSQL version | `15-alpine` |
| `databases.postgres.<name>.database` | Database name | Instance-specific |
| `databases.postgres.<name>.storage.size` | Storage size | `2Gi` |
| `databases.mongodb.comments.enabled` | Enable MongoDB | `true` |
| `databases.mongodb.comments.storage.size` | MongoDB storage | `2Gi` |

### Messaging Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `messaging.zookeeper.enabled` | Enable Zookeeper | `true` |
| `messaging.zookeeper.storage.data.size` | Zookeeper data storage | `1Gi` |
| `messaging.kafka.enabled` | Enable Kafka | `true` |
| `messaging.kafka.storage.size` | Kafka storage | `2Gi` |

### Authentication Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `auth.keycloak.enabled` | Enable Keycloak | `true` |
| `auth.keycloak.replicas` | Keycloak replicas | `1` |
| `auth.keycloak.image.tag` | Keycloak version | `23.0` |

### Monitoring Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `monitoring.prometheus.enabled` | Enable Prometheus | `false` |
| `monitoring.prometheus.storage.size` | Prometheus storage | `2Gi` |
