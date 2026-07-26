#!/bin/bash
# deploy.sh - Deploy AccessIQ application to EC2
# Usage: ./deploy.sh [jar-file-path]
#
# This script handles the deployment of the AccessIQ Spring Boot application
# to an AWS EC2 instance. It performs backup, deployment, and health checks.

set -euo pipefail

# Configuration
APP_NAME="accessiq"
DEPLOYMENT_DIR="/opt/accessiq"
SERVICE_NAME="accessiq"
HEALTH_ENDPOINT="http://localhost:8080/actuator/health"
MAX_RETRIES=30
RETRY_INTERVAL=10

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

# Check if JAR file is provided
if [ $# -eq 0 ]; then
    error_exit "No JAR file provided. Usage: $0 <jar-file-path>"
fi

JAR_FILE="$1"

if [ ! -f "$JAR_FILE" ]; then
    error_exit "JAR file not found: $JAR_FILE"
fi

log "Starting deployment of $APP_NAME..."

# Get EC2 credentials from environment
: "${EC2_HOST:?EC2_HOST environment variable not set}"
: "${EC2_USERNAME:?EC2_USERNAME environment variable not set}"
: "${EC2_SSH_KEY:?EC2_SSH_KEY environment variable not set}"

# Export SSH key for use
export SSH_KEY="$EC2_SSH_KEY"

# Create backup directory on remote server
log "Creating backup directory..."
ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
    "mkdir -p $DEPLOYMENT_DIR/backup" || error_exit "Failed to create backup directory"

# Check if previous version exists and backup
if ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
    "[ -f $DEPLOYMENT_DIR/$APP_NAME.jar ]"; then
    BACKUP_FILE="$DEPLOYMENT_DIR/backup/${APP_NAME}-$(date +%Y%m%d%H%M%S).jar"
    log "Backing up previous version..."
    ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
        "cp $DEPLOYMENT_DIR/$APP_NAME.jar $BACKUP_FILE" || log_warn "Backup failed, continuing..."
fi

# Stop application
log "Stopping application..."
ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
    "sudo systemctl stop $SERVICE_NAME || true"

# Wait for application to stop
log "Waiting for application to stop..."
sleep 5

# Copy new JAR to server
log "Copying new JAR to server..."
scp -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$JAR_FILE" \
    "$EC2_USERNAME@$EC2_HOST:$DEPLOYMENT_DIR/$APP_NAME.jar" || error_exit "Failed to copy JAR file"

# Set proper permissions
log "Setting permissions..."
ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
    "chmod 644 $DEPLOYMENT_DIR/$APP_NAME.jar"

# Start application
log "Starting application..."
ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
    "sudo systemctl start $SERVICE_NAME" || error_exit "Failed to start application"

# Wait for application to start
log "Waiting for application to start..."
sleep 10

# Health check
log "Performing health check..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    HTTP_STATUS=$(ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
        "curl -s -o /dev/null -w '%{http_code}' $HEALTH_ENDPOINT" 2>/dev/null || echo "000")
    
    if [ "$HTTP_STATUS" = "200" ]; then
        log "Application is healthy!"
        log "Deployment completed successfully!"
        exit 0
    fi
    
    RETRY_COUNT=$((RETRY_COUNT+1))
    log "Waiting for application to be healthy... (attempt $RETRY_COUNT/$MAX_RETRIES)"
    sleep $RETRY_INTERVAL
done

# Rollback on failure
log_error "Health check failed after $MAX_RETRIES attempts. Rolling back..."
./rollback.sh || log_warn "Rollback failed. Manual intervention required."

error_exit "Deployment failed. Rolled back to previous version."