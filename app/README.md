# Dockey

A microservices-based document management platform built with modern Java technologies.

## Overview

Dockey is a cloud-native application designed to handle document and user management through a microservices architecture. The system provides RESTful APIs and GraphQL endpoints for flexible data access.

## Architecture

The application consists of multiple independent microservices:

- **docs-service** - Document management and processing
- **user-service** - User authentication and profile management

Each service is independently deployable and scalable, communicating through well-defined APIs.

## Technology Stack

- **Language:** Java 11
- **Framework:** KumuluzEE (Microservices framework)
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **Containerization:** Docker

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- PostgreSQL 12+
- Docker (optional, for containerized deployment)

## Getting Started

### Local Development

1. Clone the repository
2. Configure database connections in each service's `config.yaml`
3. Build the services using Maven
4. Run each service independently

### Docker Deployment

Use the provided Docker Compose configuration for quick setup:

```bash
docker-compose up -d
```

## Services

Each service is located in its own directory with independent configuration and deployment options. Refer to individual service documentation for detailed API specifications and usage examples.