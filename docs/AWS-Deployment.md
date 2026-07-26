# AWS EC2 Deployment

This document describes how to deploy AccessIQ to AWS EC2.

## Prerequisites

1. AWS Account with appropriate permissions
2. EC2 instance (recommended: t3.medium or larger)
3. Security Groups configured
4. IAM role with necessary permissions

## Architecture

```
Internet → Load Balancer (optional) → Nginx → AccessIQ (Spring Boot) → PostgreSQL
```

## Deployment Options

### Option 1: Manual Deployment

1. Launch EC2 instance (Amazon Linux 2023 or Ubuntu 22.04)
2. Configure Security Groups:
   - SSH: Port 22 (your IP only)
   - HTTP: Port 80
   - HTTPS: Port 443
   - PostgreSQL: Port 5432 (internal only)

3. Connect via SSH and run deployment script:

```bash
# SSH into EC2
ssh -i your-key.pem ec2-user@<public-ip>

# Download and run deployment script
curl -O https://s3.amazonaws.com/your-bucket/deploy.sh
chmod +x deploy.sh
./deploy.sh
```

### Option 2: GitHub Actions Deployment

See `.github/workflows/aws-ec2.yml` for automated deployment.

## Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `EC2_HOST` | Public IP or DNS of EC2 instance |
| `EC2_USERNAME` | SSH username (ec2-user, ubuntu, etc.) |
| `EC2_SSH_KEY` | Private SSH key for EC2 access |
| `DB_PASSWORD` | PostgreSQL database password |
| `JWT_SECRET` | Secret key for JWT token signing |
| `ADMIN_EMAIL` | Admin user email |
| `ADMIN_PASSWORD` | Admin user password |

## Environment Variables

Set these environment variables on the EC2 instance:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_PASSWORD` | PostgreSQL password | - |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | - |
| `ADMIN_EMAIL` | Admin user email | admin@accessiq.com |
| `ADMIN_PASSWORD` | Admin password | - |
| `SPRING_PROFILES_ACTIVE` | Spring profile | prod |

## Security Group Configuration

### Inbound Rules

| Port | Source | Purpose |
|------|--------|---------|
| 22 | Your IP | SSH access |
| 80 | 0.0.0.0/0 | HTTP |
| 443 | 0.0.0.0/0 | HTTPS |
| 5432 | Security Group | PostgreSQL (internal) |
| 8080 | 127.0.0.1/32 | Application (internal) |

## IAM Recommendations

Create an IAM role with the following permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ec2:DescribeInstances",
                "ec2:StartInstances",
                "ec2:StopInstances",
                "ec2:RebootInstances"
            ],
            "Resource": "*"
        }
    ]
}
```

## Elastic IP

1. Allocate an Elastic IP
2. Associate with your EC2 instance
3. Update DNS records (optional)

## SSL/TLS Certificate

### Option 1: AWS Certificate Manager (ACM)

1. Request certificate in ACM for your domain
2. Validate domain ownership
3. Use with Application Load Balancer

### Option 2: Let's Encrypt

```bash
# Install Certbot
sudo apt-get install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d accessiq.yourdomain.com

# Auto-renewal
sudo crontab -e
0 12 * * * /usr/bin/certbot renew --quiet
```

## Monitoring

### Health Checks

- Application: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/prometheus`

### Alarms

Set up CloudWatch alarms for:
- CPU utilization > 80%
- Memory utilization > 85%
- Disk space < 10%
- Application health check failures

## Backup Strategy

### Database Backup

```bash
# Manual backup
pg_dump -U accessiq_user -d accessiq > backup_$(date +%Y%m%d).sql

# Automated backup script
./scripts/backup.sh
```

### Application Backup

```bash
# Using backup script
./scripts/backup.sh
```

## Rollback Procedure

```bash
# Manual rollback
./scripts/rollback.sh

# Or via GitHub Actions with workflow_dispatch
```

## Scaling

### Horizontal Scaling

Use Application Load Balancer with multiple EC2 instances:

```yaml
# docker-compose.prod.yml includes nginx for local dev
# In production, use ALB with multiple instances
```

### Vertical Scaling

Stop instance, change instance type, start:

```bash
aws ec2 stop-instances --instance-ids i-1234567890abcdef0
aws ec2 modify-instance-attribute --instance-id i-1234567890abcdef0 --instance-type t3.large
aws ec2 start-instances --instance-ids i-1234567890abcdef0
```

## Troubleshooting

### Application Not Starting

1. Check logs: `journalctl -u accessiq -f`
2. Verify JAR exists: `ls -la /opt/accessiq/`
3. Check Java version: `java -version`
4. Verify permissions: `ls -la /opt/accessiq/`

### Database Connection Issues

1. Check PostgreSQL is running: `systemctl status postgresql`
2. Verify connection: `psql -U accessiq_user -d accessiq`
3. Check network connectivity: `telnet localhost 5432`

### Nginx Issues

1. Test config: `nginx -t`
2. Check logs: `tail -f /var/log/nginx/error.log`
3. Reload: `systemctl reload nginx`

## Cost Optimization

1. Use Spot Instances for non-production
2. Use Auto Scaling Groups with scheduled scaling
3. Enable RDS storage autoscaling
4. Use S3 for static assets
5. Enable CloudWatch detailed billing