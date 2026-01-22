# Dockey

A microservices-based document management platform built with Java technologies and a React frontend.

## Overview

Dockey is a cloud-native application designed to handle document and user management through a microservices architecture. The system provides RESTful APIs and GraphQL endpoints for flexible data access.

## Architecture

The application consists of multiple independent microservices:

- **docs-service** - Document management and processing
- **user-service** - User authentication and profile management
- **comments-service** - Commenting, likes, and comment tracking
- **checker** - Inappropriate content detection (gRPC)
- **frontend** - React + TypeScript web app
- **keycloak** - Identity and access management
- **prometheus** - Monitoring and metrics

Each service is independently deployable and scalable, communicating through well-defined APIs.

## Technology Stack

- **Language:** Java 11, TypeScript
- **Framework:** KumuluzEE (Microservices), React (frontend)
- **Database:** PostgreSQL, MongoDB
- **Build Tool:** Maven, Vite
- **Containerization:** Docker

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Node.js (for frontend)
- PostgreSQL 12+
- MongoDB 6+
- Docker (optional, for containerized deployment)

## Getting Started

### Local Development

1. Clone the repository
2. Configure database connections in each service's config
3. Build the services using Maven
4. Install frontend dependencies with `npm install` in `app/frontend`
5. Run each service independently or use Docker Compose

### Docker Deployment

Use the provided Docker Compose configuration for quick setup:

```bash
docker-compose up -d
```

## Services

Each service is located in its own directory with independent configuration and deployment options. Refer to individual service documentation for detailed API specifications and usage examples.

## Deployment

### Local Development
Use Docker Compose for local development and testing (see above).

### Production Deployment to Azure Kubernetes Service (AKS)

For deploying to Azure Kubernetes Service, see the deployment guides in the repository root:

- **[DEPLOYMENT.md](../DEPLOYMENT.md)** - Comprehensive deployment guide with step-by-step instructions
- **[QUICK_START.md](../QUICK_START.md)** - Quick reference for common deployment tasks
- **[k8s/](../k8s/)** - Kubernetes manifests for all services and infrastructure

The deployment includes:
- CI/CD pipelines (GitHub Actions and Azure DevOps)
- Kubernetes manifests for all services
- Database deployments (PostgreSQL, MongoDB)
- Messaging infrastructure (Kafka, Zookeeper)
- Authentication (Keycloak)
- Monitoring (Prometheus)
