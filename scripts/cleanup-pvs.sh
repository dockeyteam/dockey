#!/bin/bash

# Script to clean up orphaned PersistentVolumes after namespace deletion
# This is useful when PVs remain after deleting a namespace

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_info "Checking for orphaned PersistentVolumes..."

# List all PVs that are Released or Failed
ORPHANED_PVS=$(kubectl get pv -o json | jq -r '.items[] | select(.status.phase == "Released" or .status.phase == "Failed") | .metadata.name' 2>/dev/null || echo "")

if [ -z "$ORPHANED_PVS" ]; then
    print_info "No orphaned PVs found in Released or Failed state."
    exit 0
fi

echo
print_warn "Found orphaned PVs:"
echo "$ORPHANED_PVS"
echo

read -p "Do you want to delete these PVs? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_info "Aborted."
    exit 0
fi

# Delete each orphaned PV
for pv in $ORPHANED_PVS; do
    print_info "Deleting PV: $pv"
    
    # Remove finalizers if present
    kubectl patch pv "$pv" -p '{"metadata":{"finalizers":null}}' 2>/dev/null || true
    
    # Delete the PV
    kubectl delete pv "$pv" --wait=false 2>/dev/null || print_warn "Failed to delete $pv (may already be deleted)"
done

print_info "Cleanup completed. Some PVs may take a few minutes to fully detach from nodes."
