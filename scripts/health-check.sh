#!/bin/bash
# health-check.sh - Check AccessIQ application health
# Usage: ./health-check.sh [endpoint-url]
#
# This script checks the health of the AccessIQ application
# by querying the Spring Boot Actuator health endpoint.

set -euo pipefail

# Configuration
APP_NAME="accessiq"
HEALTH_ENDPOINT="${1:-http://localhost:8080/actuator/health}"
TIMEOUT=10
MAX_RETRIES=3
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

# Perform health check
perform_health_check() {
    local endpoint="$1"
    local response
    local http_code
    
    response=$(curl -s -w "\n%{http_code}" --max-time "$TIMEOUT" "$endpoint" 2>/dev/null) || return 1
    http_code=$(echo "$response" | tail -n 1)
    response=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        echo "$response"
        return 0
    else
        return 1
    fi
}

# Main health check logic
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if perform_health_check "$HEALTH_ENDPOINT"; then
        log "Health check passed!"
        echo "Status: HEALTHY"
        echo "Endpoint: $HEALTH_ENDPOINT"
        exit 0
    fi
    
    RETRY_COUNT=$((RETRY_COUNT+1))
    log_warn "Health check attempt $RETRY_COUNT failed, retrying..."
    sleep $RETRY_INTERVAL
done

log_error "Health check failed after $MAX_RETRIES attempts"
echo "Status: UNHEALTHY"
echo "Endpoint: $HEALTH_ENDPOINT"
exit 1