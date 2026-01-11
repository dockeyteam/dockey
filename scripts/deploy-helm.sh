#!/bin/bash

# Dockey Helm Deployment Script
# This script helps deploy the Dockey application using Helm

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
RELEASE_NAME="dockey"
NAMESPACE="dockey"
CHART_PATH="./helm/dockey"

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
    
    if ! command -v helm &> /dev/null; then
        print_error "Helm is not installed. Please install Helm 3.0+ first."
        exit 1
    fi
    
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed. Please install it first."
        exit 1
    fi
    
    if ! command -v az &> /dev/null; then
        print_error "Azure CLI is not installed. Please install it first."
        exit 1
    fi
    
    # Check Helm version
    HELM_VERSION=$(helm version --short | cut -d' ' -f1 | sed 's/v//')
    if [[ $(echo "$HELM_VERSION 3.0" | awk '{print ($1 >= $2)}') -eq 0 ]]; then
        print_error "Helm 3.0+ is required. Current version: $HELM_VERSION"
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
    print_info "Creating namespace if it doesn't exist..."
    
    # Check if namespace exists
    if kubectl get namespace ${NAMESPACE} &> /dev/null; then
        print_info "Namespace ${NAMESPACE} already exists."
    else
        kubectl create namespace ${NAMESPACE}
        print_info "Namespace ${NAMESPACE} created."
    fi
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
        print_info "See k8s/secrets/README.md or helm/dockey/README.md for instructions."
        read -p "Continue anyway? (y/N) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    else
        print_info "All required secrets are present."
    fi
}

deploy_with_helm() {
    local image_tag="${1:-latest}"
    local values_file="${2:-}"
    
    print_info "Deploying with Helm..."
    print_info "Release name: ${RELEASE_NAME}"
    print_info "Namespace: ${NAMESPACE}"
    print_info "Image tag: ${image_tag}"
    if [ -n "${values_file}" ]; then
        print_info "Using values file: ${values_file}"
    fi
    
    # Build helm command arguments
    local helm_args=(
        "-n" "${NAMESPACE}"
        "--create-namespace"
        "--set" "services.comments.image.tag=${image_tag}"
        "--set" "services.docs.image.tag=${image_tag}"
        "--set" "services.user.image.tag=${image_tag}"
        "--wait"
        "--timeout" "15m"
    )
    
    # Add values file if provided
    if [ -n "${values_file}" ] && [ -f "${values_file}" ]; then
        helm_args+=("-f" "${values_file}")
    fi
    
    # Check if release exists
    if helm list -n ${NAMESPACE} | grep -q ${RELEASE_NAME}; then
        print_info "Release ${RELEASE_NAME} exists. Upgrading..."
        helm upgrade ${RELEASE_NAME} ${CHART_PATH} "${helm_args[@]}"
    else
        print_info "Installing new release ${RELEASE_NAME}..."
        helm install ${RELEASE_NAME} ${CHART_PATH} "${helm_args[@]}"
    fi
    
    print_info "Helm deployment completed."
}

show_status() {
    print_info "Deployment status:"
    echo
    helm status ${RELEASE_NAME} -n ${NAMESPACE}
    echo
    print_info "Pods:"
    kubectl get pods -n ${NAMESPACE}
    echo
    print_info "Services:"
    kubectl get svc -n ${NAMESPACE}
    echo
    print_info "To view logs: kubectl logs -f <pod-name> -n ${NAMESPACE}"
    print_info "To port forward: kubectl port-forward svc/<service-name> <local-port>:<service-port> -n ${NAMESPACE}"
}

# Main execution
main() {
    local image_tag="${1:-latest}"
    local values_file="${2:-}"
    
    print_info "Starting Dockey Helm deployment to AKS..."
    echo
    
    check_prerequisites
    create_namespace
    check_secrets
    deploy_with_helm ${image_tag} ${values_file}
    
    echo
    show_status
    echo
    print_info "Deployment completed!"
}

# Run main function
main "$@"
