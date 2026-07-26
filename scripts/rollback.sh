#!/bin/bash
# rollback.sh - Rollback AccessIQ application to previous version
# Usage: ./rollback.sh [version-timestamp]
#
# This script rolls back the AccessIQ application to a previous version
# by restoring from backup and restarting the service.

set -euo pipefail

# Configuration
APP_NAME="accessiq"
DEPLOYMENT_DIR="/opt/accessiq"
SERVICE_NAME="accessiq"
BACKUP_DIR="$DEPLOYMENT_DIR/backup"
HEALTH_ENDPOINT="http://localhost:8080/actuator/health"
MAX_RETRIES=15
RETRY_INTERVAL=5

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
: "${EC2_HOST:?EC2_HOST environment variable not set}"
: "${EC2_USERNAME:?EC2_USERNAME environment variable not set}"
: "${EC2_SSH_KEY:?EC2_SSH_KEY environment variable not set}"

# Export SSH key for use
export SSH_KEY="$EC2_SSH_KEY"

log "Starting rollback process for $APP_NAME..."

# Check if a specific version is provided
if [ $# -eq 1 ]; then
    VERSION_TIMESTAMP="$1"
    BACKUP_FILE="$BACKUP_DIR/${APP_NAME}-${VERSION_TIMESTAMP}.jar"
else
    # Find the most recent backup
    BACKUP_FILE=$(ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
        "ls -t $BACKUP_DIR/${APP_NAME}-*.jar 2>/dev/null | head -1" || echo "")
fi

if [ -z "$BACKUP_FILE" ]; then
    error_exit "No backup file found for rollback"
fi

log "Restoring from backup: $BACKUP_FILE"

# Stop application
log "Stopping application..."
ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
    "sudo systemctl stop $SERVICE_NAME || true"

# Wait for application to stop
sleep 3

# Restore backup
log "Restoring backup..."
ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
    "cp $BACKUP_FILE $DEPLOYMENT_DIR/$APP_NAME.jar" || error_exit "Failed to restore backup"

# Set proper permissions
ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
    "chmod 644 $DEPLOYMENT_DIR/$APP_NAME.jar"

# Start application
log "Starting application..."
ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
    "sudo systemctl start $SERVICE_NAME" || error_exit "Failed to start application"

# Wait for application to start
log "Waiting for application to start..."
sleep 5

# Health check
log "Performing health check..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    HTTP_STATUS=$(ssh -o StrictHostKeyChecking=no -i <(echo "$SSH_KEY") "$EC2_USERNAME@$EC2_HOST" \
        "curl -s -o /dev/null -w '%{http_code}' $HEALTH_ENDPOINT" 2>/dev/null || echo "000")
    
    if [ "$HTTP_STATUS" = "200" ]; then
        log "Application is healthy after rollback!"
        log "Rollback completed successfully!"
        exit 0
    fi
    
    RETRY_COUNT=$((RETRY_COUNT+1))
    log "Waiting for application to be healthy... (attempt $RETRY_COUNT/$MAX_RETRIES)"
    sleep $RETRY_INTERVAL
done

error_exit "Rollback failed. Health check did not pass."