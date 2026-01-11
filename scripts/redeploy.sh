#!/bin/bash

# Complete Redeployment Script for Dockey
# This script uninstalls and reinstalls the entire Helm release

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
    
    # Check kubectl connection
    if ! kubectl cluster-info &> /dev/null; then
        print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
        exit 1
    fi
    
    print_info "Prerequisites check passed."
}

setup_acr_secret() {
    print_info "Setting up ACR imagePullSecret..."
    
    # Check if secret already exists
    if kubectl get secret acr-secret -n ${NAMESPACE} &> /dev/null; then
        print_info "ACR secret already exists. Updating..."
        kubectl delete secret acr-secret -n ${NAMESPACE}
    fi
    
    # Get ACR credentials
    ACR_USERNAME=$(az acr credential show --name dockey --query username -o tsv)
    ACR_PASSWORD=$(az acr credential show --name dockey --query 'passwords[0].value' -o tsv)
    
    # Create secret
    kubectl create secret docker-registry acr-secret \
        --docker-server=dockey.azurecr.io \
        --docker-username=${ACR_USERNAME} \
        --docker-password=${ACR_PASSWORD} \
        -n ${NAMESPACE}
    
    print_info "ACR secret created/updated."
}

uninstall_release() {
    print_info "Uninstalling existing Helm release..."
    
    if helm list -n ${NAMESPACE} | grep -q ${RELEASE_NAME}; then
        print_warn "Uninstalling release: ${RELEASE_NAME}"
        helm uninstall ${RELEASE_NAME} -n ${NAMESPACE} --wait --timeout 5m || true
        print_info "Release uninstalled."
        
        # Wait a bit for resources to be cleaned up
        print_info "Waiting for resources to be cleaned up (30 seconds)..."
        sleep 30
    else
        print_info "No existing release found."
    fi
}

install_release() {
    local image_tag="${1:-latest}"
    local values_file="${2:-}"
    
    print_info "Installing Helm release..."
    print_info "Release name: ${RELEASE_NAME}"
    print_info "Namespace: ${NAMESPACE}"
    print_info "Image tag: ${image_tag}"
    
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
        print_info "Using values file: ${values_file}"
    fi
    
    helm install ${RELEASE_NAME} ${CHART_PATH} "${helm_args[@]}"
    
    print_info "Helm installation completed."
}

show_status() {
    print_info "Deployment status:"
    echo
    helm status ${RELEASE_NAME} -n ${NAMESPACE} 2>/dev/null || print_warn "Release not found"
    echo
    print_info "Pods:"
    kubectl get pods -n ${NAMESPACE}
    echo
    print_info "Services:"
    kubectl get svc -n ${NAMESPACE}
    echo
    print_info "PersistentVolumeClaims:"
    kubectl get pvc -n ${NAMESPACE}
}

# Main execution
main() {
    local image_tag="${1:-latest}"
    local values_file="${2:-}"
    
    print_info "Starting complete redeployment of Dockey..."
    echo
    
    check_prerequisites
    
    # Ensure namespace exists
    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
    
    # Setup ACR secret
    setup_acr_secret
    
    # Uninstall existing release
    uninstall_release
    
    # Install fresh release
    install_release ${image_tag} ${values_file}
    
    echo
    show_status
    echo
    print_info "Redeployment completed!"
    print_info "Monitor pods with: kubectl get pods -n ${NAMESPACE} -w"
}

# Run main function
main "$@"
