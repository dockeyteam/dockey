#!/bin/bash

# Script to grant AKS cluster access to Azure Container Registry (ACR)
# This fixes "401 Unauthorized" errors when pulling images from ACR

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
AKS_RESOURCE_GROUP="FRItest"
AKS_CLUSTER_NAME="dockey1"
ACR_NAME="dockey"

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
    
    if ! command -v az &> /dev/null; then
        print_error "Azure CLI is not installed. Please install it first."
        exit 1
    fi
    
    # Check if logged in to Azure
    if ! az account show &> /dev/null; then
        print_warn "Not logged in to Azure. Attempting to login..."
        az login
    fi
    
    print_info "Prerequisites check passed."
}

setup_acr_access() {
    print_info "Setting up ACR access for AKS cluster..."
    
    # Get AKS managed identity
    print_info "Getting AKS cluster identity..."
    AKS_ID=$(az aks show \
        --resource-group ${AKS_RESOURCE_GROUP} \
        --name ${AKS_CLUSTER_NAME} \
        --query "identity.principalId" \
        --output tsv)
    
    if [ -z "$AKS_ID" ]; then
        print_error "Failed to get AKS managed identity. Is the cluster using SystemAssigned identity?"
        exit 1
    fi
    
    print_info "AKS Managed Identity: ${AKS_ID}"
    
    # Get ACR resource ID
    print_info "Getting ACR resource ID..."
    ACR_ID=$(az acr show \
        --name ${ACR_NAME} \
        --query "id" \
        --output tsv)
    
    if [ -z "$ACR_ID" ]; then
        print_error "Failed to get ACR resource ID. Does ACR '${ACR_NAME}' exist?"
        exit 1
    fi
    
    print_info "ACR Resource ID: ${ACR_ID}"
    
    # Check if role assignment already exists
    print_info "Checking for existing role assignment..."
    EXISTING=$(az role assignment list \
        --assignee ${AKS_ID} \
        --scope ${ACR_ID} \
        --query "[?roleDefinitionName=='AcrPull'].id" \
        --output tsv 2>/dev/null || echo "")
    
    if [ -n "$EXISTING" ]; then
        print_warn "Role assignment already exists. Skipping..."
        print_info "Role assignment ID: ${EXISTING}"
    else
        # Grant AcrPull role
        print_info "Granting AcrPull role to AKS identity..."
        az role assignment create \
            --assignee ${AKS_ID} \
            --role AcrPull \
            --scope ${ACR_ID} \
            --output none
        
        print_info "Successfully granted AcrPull role!"
    fi
    
    # Wait a moment for propagation
    print_info "Waiting for role assignment to propagate (10 seconds)..."
    sleep 10
    
    print_info "ACR access setup completed!"
    print_info "You may need to restart pods to pick up the new permissions."
    print_info "Run: kubectl rollout restart deployment -n dockey"
}

# Main execution
main() {
    print_info "Starting ACR access setup for AKS..."
    echo
    
    check_prerequisites
    setup_acr_access
    
    echo
    print_info "Setup completed!"
    print_info "If pods are still failing, restart them:"
    print_info "  kubectl rollout restart deployment -n dockey"
}

# Run main function
main
