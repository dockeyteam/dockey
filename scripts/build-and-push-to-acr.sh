#!/bin/bash

# Build and Push Docker Images to Azure Container Registry
# This script builds all service images and pushes them to ACR

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
ACR_NAME="dockey"
REGISTRY="${ACR_NAME}.azurecr.io"
IMAGE_TAG="${1:-latest}"

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
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
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

login_to_acr() {
    print_info "Logging in to Azure Container Registry: ${ACR_NAME}..."
    
    if az acr login --name ${ACR_NAME} 2>/dev/null; then
        print_info "Successfully logged in to ACR."
    else
        print_error "Failed to login to ACR. Please check:"
        print_error "  1. ACR name is correct: ${ACR_NAME}"
        print_error "  2. You have permissions to access ACR"
        print_error "  3. ACR exists in your subscription"
        exit 1
    fi
}

build_and_push_image() {
    local service_name=$1
    local context_path=$2
    local dockerfile_path="${context_path}/Dockerfile"
    
    print_info "Building ${service_name}..."
    
    if [ ! -f "$dockerfile_path" ]; then
        print_error "Dockerfile not found at ${dockerfile_path}"
        return 1
    fi
    
    # Build the image for linux/amd64 platform (AKS uses AMD64 nodes)
    docker build --platform linux/amd64 \
                 -t ${REGISTRY}/${service_name}:${IMAGE_TAG} \
                 -t ${REGISTRY}/${service_name}:latest \
                 -f ${dockerfile_path} \
                 ${context_path}
    
    if [ $? -eq 0 ]; then
        print_info "Successfully built ${service_name}"
    else
        print_error "Failed to build ${service_name}"
        return 1
    fi
    
    # Push the image
    print_info "Pushing ${service_name} to ACR..."
    docker push ${REGISTRY}/${service_name}:${IMAGE_TAG}
    docker push ${REGISTRY}/${service_name}:latest
    
    if [ $? -eq 0 ]; then
        print_info "Successfully pushed ${service_name}:${IMAGE_TAG}"
        print_info "Successfully pushed ${service_name}:latest"
    else
        print_error "Failed to push ${service_name}"
        return 1
    fi
}

verify_images() {
    print_info "Verifying images in ACR..."
    
    echo
    print_info "Repositories in ACR:"
    az acr repository list --name ${ACR_NAME} --output table
    
    echo
    print_info "Image tags:"
    for service in comments-service docs-service user-service; do
        echo
        print_info "Tags for ${service}:"
        az acr repository show-tags --name ${ACR_NAME} --repository ${service} --output table 2>/dev/null || print_warn "No tags found for ${service}"
    done
}

# Main execution
main() {
    print_info "Starting build and push process to ACR..."
    print_info "ACR: ${REGISTRY}"
    print_info "Image tag: ${IMAGE_TAG}"
    echo
    
    check_prerequisites
    login_to_acr
    
    # Build and push each service
    build_and_push_image "comments-service" "./app/comments-service"
    build_and_push_image "docs-service" "./app/docs-service"
    build_and_push_image "user-service" "./app/user-service"
    
    echo
    verify_images
    
    echo
    print_info "Build and push completed successfully!"
    print_info "You can now deploy to AKS using the Kubernetes manifests."
}

# Run main function
main
