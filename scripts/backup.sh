#!/bin/bash
# backup.sh - Backup AccessIQ application
# Usage: ./backup.sh [backup-dir]
#
# This script creates a backup of the AccessIQ application JAR file
# and optionally creates a database backup.

set -euo pipefail

# Configuration
APP_NAME="accessiq"
DEPLOYMENT_DIR="${DEPLOYMENT_DIR:-/opt/accessiq}"
BACKUP_DIR="${1:-$DEPLOYMENT_DIR/backup}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

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

# Get EC2 credentials from environment
if [ -n "${EC2_HOST:-}" ]; then
    : "${EC2_USERNAME:?EC2_USERNAME environment variable not set}"
    : "${EC2_SSH_KEY:?EC2_SSH_KEY environment variable not set}"
    SSH_OPTIONS="-o StrictHostKeyChecking=no -i <(echo "$EC2_SSH_KEY")"
else
    log_warn "Running locally - no SSH connection will be made"
    SSH_OPTIONS=""
fi

log "Creating backup of $APP_NAME..."

# Create backup directory
if [ -n "${EC2_HOST:-}" ]; then
    ssh $SSH_OPTIONS "$EC2_USERNAME@$EC2_HOST" "mkdir -p $BACKUP_DIR"
else
    mkdir -p "$BACKUP_DIR"
fi

# Get current JAR file
if [ -n "${EC2_HOST:-}" ]; then
    CURRENT_JAR="$BACKUP_DIR/${APP_NAME}_${TIMESTAMP}.jar"
    ssh $SSH_OPTIONS "$EC2_USERNAME@$EC2_HOST" \
        "if [ -f $DEPLOYMENT_DIR/$APP_NAME.jar ]; then cp $DEPLOYMENT_DIR/$APP_NAME.jar $CURRENT_JAR; fi"
else
    CURRENT_JAR="$BACKUP_DIR/${APP_NAME}_${TIMESTAMP}.jar"
    if [ -f "$DEPLOYMENT_DIR/$APP_NAME.jar" ]; then
        cp "$DEPLOYMENT_DIR/$APP_NAME.jar" "$CURRENT_JAR"
    fi
fi

# Verify backup
if [ -f "$CURRENT_JAR" ]; then
    SIZE=$(stat -c%s "$CURRENT_JAR" 2>/dev/null || stat -f%z "$CURRENT_JAR" 2>/dev/null)
    log "Backup created successfully: $CURRENT_JAR ($SIZE bytes)"
else
    log_warn "No current JAR file found to backup"
fi

# List existing backups
log "Existing backups:"
if [ -n "${EC2_HOST:-}" ]; then
    ssh $SSH_OPTIONS "$EC2_USERNAME@$EC2_HOST" "ls -lh $BACKUP_DIR/${APP_NAME}_*.jar 2>/dev/null || echo 'No backups found'"
else
    ls -lh "$BACKUP_DIR"/${APP_NAME}_*.jar 2>/dev/null || echo "No backups found"
fi

log "Backup completed!"