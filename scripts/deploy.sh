#!/bin/bash

# Dockey AKS Deployment Script
# This script helps deploy the Dockey application to AKS

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="dockey"
K8S_DIR="./k8s"

# Functions
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    print_info "Checking prerequisites..."
    
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed. Please install it first."
        exit 1
    fi
    
    if ! command -v az &> /dev/null; then
        print_error "Azure CLI is not installed. Please install it first."
        exit 1
    fi
    
    # Check kubectl connection
    if ! kubectl cluster-info &> /dev/null; then
        print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
        exit 1
    fi
    
    print_info "Prerequisites check passed."
}

create_namespace() {
    print_info "Creating namespace..."
    kubectl apply -f ${K8S_DIR}/namespace.yaml
    print_info "Namespace created."
}

check_secrets() {
    print_info "Checking for required secrets..."
    
    REQUIRED_SECRETS=(
        "postgres-docs-secret"
        "postgres-users-secret"
        "postgres-keycloak-secret"
        "mongodb-comments-secret"
        "keycloak-admin-secret"
    )
    
    MISSING_SECRETS=()
    
    for secret in "${REQUIRED_SECRETS[@]}"; do
        if ! kubectl get secret ${secret} -n ${NAMESPACE} &> /dev/null; then
            MISSING_SECRETS+=(${secret})
        fi
    done
    
    if [ ${#MISSING_SECRETS[@]} -gt 0 ]; then
        print_warn "Missing secrets: ${MISSING_SECRETS[*]}"
        print_warn "Please create these secrets before proceeding."
        print_info "See k8s/secrets/README.md for instructions."
        read -p "Continue anyway? (y/N) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        print_info "All required secrets are present."
    fi
}

deploy_databases() {
    print_info "Deploying databases..."
    kubectl apply -f ${K8S_DIR}/databases/
    
    print_info "Waiting for databases to be ready..."
    kubectl wait --for=condition=ready pod -l app=postgres-docs -n ${NAMESPACE} --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=postgres-users -n ${NAMESPACE} --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=postgres-keycloak -n ${NAMESPACE} --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=mongodb-comments -n ${NAMESPACE} --timeout=300s || true
    print_info "Databases deployed."
}

deploy_messaging() {
    print_info "Deploying messaging infrastructure..."
    kubectl apply -f ${K8S_DIR}/messaging/
    
    print_info "Waiting for Zookeeper to be ready..."
    kubectl wait --for=condition=ready pod -l app=zookeeper -n ${NAMESPACE} --timeout=300s || true
    
    print_info "Waiting for Kafka to be ready..."
    kubectl wait --for=condition=ready pod -l app=kafka -n ${NAMESPACE} --timeout=300s || true
    print_info "Messaging infrastructure deployed."
}

deploy_auth() {
    print_info "Deploying Keycloak..."
    kubectl apply -f ${K8S_DIR}/auth/
    
    print_info "Waiting for Keycloak to be ready (this may take a few minutes)..."
    kubectl wait --for=condition=ready pod -l app=keycloak -n ${NAMESPACE} --timeout=600s || true
    print_info "Keycloak deployed."
}

deploy_services() {
    print_info "Deploying application services..."
    kubectl apply -f ${K8S_DIR}/services/
    
    print_info "Waiting for services to be ready..."
    kubectl wait --for=condition=available deployment/comments-service -n ${NAMESPACE} --timeout=300s || true
    kubectl wait --for=condition=available deployment/docs-service -n ${NAMESPACE} --timeout=300s || true
    kubectl wait --for=condition=available deployment/user-service -n ${NAMESPACE} --timeout=300s || true
    print_info "Application services deployed."
}

deploy_monitoring() {
    print_info "Deploying monitoring..."
    kubectl apply -f ${K8S_DIR}/monitoring/
    print_info "Monitoring deployed."
}

deploy_ingress() {
    read -p "Deploy ingress? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Deploying ingress..."
        kubectl apply -f ${K8S_DIR}/ingress/
        print_info "Ingress deployed."
    fi
}

show_status() {
    print_info "Deployment status:"
    echo
    kubectl get pods -n ${NAMESPACE}
    echo
    kubectl get svc -n ${NAMESPACE}
    echo
    print_info "To view logs: kubectl logs -f <pod-name> -n ${NAMESPACE}"
    print_info "To port forward: kubectl port-forward svc/<service-name> <local-port>:<service-port> -n ${NAMESPACE}"
}

# Main execution
main() {
    print_info "Starting Dockey deployment to AKS..."
    echo
    
    check_prerequisites
    create_namespace
    check_secrets
    deploy_databases
    deploy_messaging
    deploy_auth
    deploy_services
    deploy_monitoring
    deploy_ingress
    
    echo
    show_status
    echo
    print_info "Deployment completed!"
}

# Run main function
main
