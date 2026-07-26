#!/bin/bash
# ec2-user-data.sh - EC2 User Data script for AccessIQ deployment
# This script is executed when the EC2 instance boots for the first time
#
# Usage: This script is meant to be used as EC2 User Data

set -euo pipefail

# Configuration
APP_NAME="accessiq"
APP_VERSION="1.0.0"
DEPLOYMENT_DIR="/opt/accessiq"
SERVICE_NAME="accessiq"
JAVA_VERSION="17"

# Logging
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

error_exit() {
    log "ERROR: $1"
    exit 1
}

log "Starting AccessIQ EC2 setup..."

# Update system packages
log "Updating system packages..."
apt-get update -y || yum update -y

# Install Java 17
log "Installing Java ${JAVA_VERSION}..."
if command -v apt-get &> /dev/null; then
    apt-get install -y openjdk-${JAVA_VERSION}-jdk curl wget git
    update-alternatives --set java /usr/lib/jvm/java-${JAVA_VERSION}-openjdk-amd64/bin/java
elif command -v yum &> /dev/null; then
    amazon-linux-extras install java-17-amazon-corretto -y
fi

# Verify Java installation
JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export JAVA_HOME
log "Java installed at: $JAVA_HOME"

# Create application user
log "Creating application user..."
if ! id -u accessiq &>/dev/null; then
    useradd -r -s /bin/false -d /opt/accessiq accessiq
fi

# Create deployment directory
log "Creating deployment directory..."
mkdir -p "$DEPLOYMENT_DIR"
mkdir -p "$DEPLOYMENT_DIR/backup"
mkdir -p "$DEPLOYMENT_DIR/logs"

# Download application (replace with your S3 bucket or GitHub release URL)
# APPLICATION_URL="https://github.com/your-org/accessiq/releases/download/v${APP_VERSION}/accessiq-${APP_VERSION}.jar"
# wget -O "$DEPLOYMENT_DIR/$APP_NAME.jar" "$APPLICATION_URL"

# For demonstration, we'll just create a placeholder
# In production, download from S3 or GitHub releases
log "Note: In production, download the JAR from S3 or GitHub releases"
log "Example: wget -O $DEPLOYMENT_DIR/$APP_NAME.jar <your-jar-url>"

# Set permissions
log "Setting permissions..."
chown -R accessiq:accessiq "$DEPLOYMENT_DIR"
chmod 755 "$DEPLOYMENT_DIR"

# Create systemd service file
log "Creating systemd service..."
cat > /etc/systemd/system/$SERVICE_NAME.service << 'EOF'
[Unit]
Description=AccessIQ Spring Boot Application
After=network.target network-online.target
Wants=network-online.target

[Service]
Type=simple
User=accessiq
Group=accessiq
WorkingDirectory=/opt/accessiq
Environment="JAVA_OPTS=-Xms256m -Xmx1024m -XX:+UseG1GC"
Environment="SPRING_PROFILES_ACTIVE=prod"
ExecStart=/usr/bin/java $JAVA_OPTS -jar /opt/accessiq/accessiq.jar
Restart=on-failure
RestartSec=30
TimeoutStartSec=120
TimeoutStopSec=60

[Install]
WantedBy=multi-user.target
EOF

# Create systemd drop-in directory for environment variables
log "Creating systemd drop-in configuration..."
mkdir -p /etc/systemd/system/$SERVICE_NAME.service.d
cat > /etc/systemd/system/$SERVICE_NAME.service.d/override.conf << 'EOF'
[Service]
EnvironmentFile=/opt/accessiq/application.properties
EOF

# Create application properties file (update with your values)
log "Creating application properties..."
cat > "$DEPLOYMENT_DIR/application.properties" << EOF
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/accessiq
spring.datasource.username=accessiq_user
spring.datasource.password=${DB_PASSWORD}

# JWT Configuration
accessiq.jwt.secret=${JWT_SECRET}
accessiq.jwt.access-token-minutes=15
accessiq.jwt.refresh-token-days=7

# Admin Credentials
accessiq.bootstrap.admin.email=${ADMIN_EMAIL}
accessiq.bootstrap.admin.password=${ADMIN_PASSWORD}
EOF

# Enable and start service
log "Enabling and starting service..."
systemctl daemon-reload
systemctl enable $SERVICE_NAME
# systemctl start $SERVICE_NAME  # Uncomment when JAR is available

# Install and configure Nginx (optional)
log "Installing Nginx..."
if command -v apt-get &> /dev/null; then
    apt-get install -y nginx
elif command -v yum &> /dev/null; then
    amazon-linux-extras install nginx1 -y
fi

# Create Nginx configuration
log "Configuring Nginx..."
cat > /etc/nginx/sites-available/accessiq << 'NGINX_CONF'
upstream accessiq_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name _;
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
    
    location / {
        proxy_pass http://accessiq_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /actuator/health {
        proxy_pass http://accessiq_backend;
        access_log off;
    }
}
NGINX_CONF

# Enable site
ln -sf /etc/nginx/sites-available/accessiq /etc/nginx/sites-enabled/accessiq
rm -f /etc/nginx/sites-enabled/default
systemctl enable nginx
# systemctl start nginx  # Uncomment when ready

# Configure firewall
log "Configuring firewall..."
if command -v ufw &> /dev/null; then
    ufw allow ssh
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw allow 5432/tcp
    ufw --force enable
elif command -v firewall-cmd &> /dev/null; then
    firewall-cmd --permanent --add-service=ssh
    firewall-cmd --permanent --add-service=http
    firewall-cmd --permanent --add-service=https
    firewall-cmd --reload
fi

# Create swap if needed
if [ $(free -m | awk '/^Swap:/ {print $2}') -eq 0 ]; then
    log "Creating swap space..."
    fallocate -l 1G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

log "AccessIQ EC2 setup completed!"
log "Next steps:"
log "1. Download the application JAR to $DEPLOYMENT_DIR"
log "2. Set environment variables: DB_PASSWORD, JWT_SECRET, ADMIN_EMAIL, ADMIN_PASSWORD"
log "3. Run: systemctl start $SERVICE_NAME"
log "4. Verify: curl http://localhost:8080/actuator/health"