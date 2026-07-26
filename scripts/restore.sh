#!/bin/bash
# restore.sh - Restore AccessIQ application from backup
# Usage: ./restore.sh [backup-file-path]
#
# This script restores the AccessIQ application from a backup file.

set -euo pipefail

# Configuration
APP_NAME="accessiq"
DEPLOYMENT_DIR="${DEPLOYMENT_DIR:-/opt/accessiq}"
SERVICE_NAME="accessiq"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR:${NC} $1" >&2
}

log_warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARN:${NC} $1"
}

# Error handling
error_exit() {
    log_error "$1"
    exit 1
}

# Get EC2 credentials from environment
if [ -n "${EC2_HOST:-}" ]; then
    : "${EC2_USERNAME:?EC2_USERNAME environment variable not set}"
    : "${EC2_SSH_KEY:?EC2_SSH_KEY environment variable not set}"
    SSH_OPTIONS="-o StrictHostKeyChecking=no -i <(echo "$EC2_SSH_KEY")"
else
    SSH_OPTIONS=""
fi

# Check if backup file is provided
if [ $# -eq 0 ]; then
    if [ -n "${EC2_HOST:-}" ]; then
        log "Listing available backups on remote server..."
        ssh $SSH_OPTIONS "$EC2_USERNAME@$EC2_HOST" "ls -la $DEPLOYMENT_DIR/backup/${APP_NAME}_*.jar" || log_warn "No backups found"
        error_exit "Please provide a backup file path"
    else
        log "Listing available backups locally..."
        ls -la "$DEPLOYMENT_DIR/backup"/${APP_NAME}_*.jar 2>/dev/null || log_warn "No backups found"
        error_exit "Please provide a backup file path"
    fi
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
    error_exit "Backup file not found: $BACKUP_FILE"
fi

log "Restoring from backup: $BACKUP_FILE"

# Stop application
log "Stopping application..."
if [ -n "${EC2_HOST:-}" ]; then
    ssh $SSH_OPTIONS "$EC2_USERNAME@$EC2_HOST" "sudo systemctl stop $SERVICE_NAME || true"
else
    sudo systemctl stop $SERVICE_NAME || true
fi

sleep 2

# Copy backup to deployment directory
log "Restoring JAR file..."
if [ -n "${EC2_HOST:-}" ]; then
    scp $SSH_OPTIONS "$BACKUP_FILE" "$EC2_USERNAME@$EC2_HOST:$DEPLOYMENT_DIR/$APP_NAME.jar"
    ssh $SSH_OPTIONS "$EC2_USERNAME@$EC2_HOST" "chmod 644 $DEPLOYMENT_DIR/$APP_NAME.jar"
else
    cp "$BACKUP_FILE" "$DEPLOYMENT_DIR/$APP_NAME.jar"
    chmod 644 "$DEPLOYMENT_DIR/$APP_NAME.jar"
fi

# Start application
log "Starting application..."
if [ -n "${EC2_HOST:-}" ]; then
    ssh $SSH_OPTIONS "$EC2_USERNAME@$EC2_HOST" "sudo systemctl start $SERVICE_NAME"
else
    sudo systemctl start $SERVICE_NAME
fi

sleep 5

# Verify restore
log "Verifying restore..."
if [ -n "${EC2_HOST:-}" ]; then
    ssh $SSH_OPTIONS "$EC2_USERNAME@$EC2_HOST" "sudo systemctl status $SERVICE_NAME"
    curl -s http://localhost:8080/actuator/health | head -c 100
else
    sudo systemctl status $SERVICE_NAME
    curl -s http://localhost:8080/actuator/health | head -c 100
fi

log "Restore completed successfully!"