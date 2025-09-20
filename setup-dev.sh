#!/bin/bash

# Freelancer Platform - Development Setup Script
# This script sets up the development environment

set -e

echo "🔧 Freelancer Platform - Development Setup"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo -e "${BLUE}📋 Checking prerequisites...${NC}"

# Check Docker
if command_exists docker; then
    echo -e "${GREEN}✅ Docker found$(NC)"
else
    echo -e "${RED}❌ Docker not found. Please install Docker first.${NC}"
    echo "Visit: https://docs.docker.com/get-docker/"
    exit 1
fi

# Check Docker Compose
if command_exists docker-compose || docker compose version >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Docker Compose found${NC}"
else
    echo -e "${RED}❌ Docker Compose not found. Please install Docker Compose.${NC}"
    exit 1
fi

# Check Java
if command_exists java; then
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}✅ Java found (version $java_version)${NC}"
else
    echo -e "${YELLOW}⚠️  Java not found. Maven wrapper will handle this.${NC}"
fi

# Make scripts executable
echo -e "${BLUE}🔧 Setting up scripts...${NC}"
chmod +x build-all.sh
chmod +x deploy.sh

# Create data directories if they don't exist
echo -e "${BLUE}📁 Creating data directories...${NC}"
mkdir -p data/postgres
mkdir -p data/redis
mkdir -p data/kafka
mkdir -p logs

# Set up environment variables
echo -e "${BLUE}⚙️  Setting up environment...${NC}"
if [ ! -f ".env" ]; then
    cat > .env << EOF
# Freelancer Platform Environment Configuration

# Database Configuration
POSTGRES_DB=freelancer_db
POSTGRES_USER=freelancer
POSTGRES_PASSWORD=freelancer123
POSTGRES_HOST=localhost

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# JWT Configuration (change in production)
JWT_SECRET=your-super-secret-jwt-key-change-in-production
JWT_EXPIRATION=86400000

# Stripe Configuration (add your keys)
STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key

# Cloudinary Configuration (add your credentials)
CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_API_SECRET=your_cloudinary_api_secret

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
LOG_LEVEL=INFO

# External Service URLs
AUTH_SERVICE_URL=http://localhost:8081
GIG_SERVICE_URL=http://localhost:8082
JOB_PROPOSAL_SERVICE_URL=http://localhost:8083
WORKSPACE_SERVICE_URL=http://localhost:8084
NOTIFICATION_SERVICE_URL=http://localhost:8085
PAYMENT_SERVICE_URL=http://localhost:8086
AI_SERVICE_URL=http://localhost:8087
API_GATEWAY_URL=http://localhost:8080
EOF
    echo -e "${GREEN}✅ Created .env file${NC}"
    echo -e "${YELLOW}⚠️  Please update the .env file with your actual credentials${NC}"
else
    echo -e "${YELLOW}⚠️  .env file already exists${NC}"
fi

# Create development docker-compose override
echo -e "${BLUE}🐳 Creating development Docker Compose override...${NC}"
if [ ! -f "docker-compose.override.yml" ]; then
    cat > docker-compose.override.yml << EOF
version: '3.8'

# Development overrides for docker-compose.yml
# This file is automatically used by Docker Compose in development

services:
  # Development-specific configurations
  
  # Enable debug ports for all services
  auth-service:
    environment:
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
    ports:
      - "5005:5005"  # Debug port
      
  gig-service:
    environment:
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006
    ports:
      - "5006:5006"  # Debug port
      
  job-proposal-service:
    environment:
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007
    ports:
      - "5007:5007"  # Debug port
      
  workspace-service:
    environment:
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008
    ports:
      - "5008:5008"  # Debug port
      
  notification-service:
    environment:
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5009
    ports:
      - "5009:5009"  # Debug port
      
  payment-service:
    environment:
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5010
    ports:
      - "5010:5010"  # Debug port
      
  ai-service:
    environment:
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5011
    ports:
      - "5011:5011"  # Debug port

  # Mount source code for hot reload (if needed)
  # Uncomment and modify as needed for development
  # auth-service:
  #   volumes:
  #     - ./auth-service/src:/app/src
      
  # Enable more verbose logging
  postgres-auth:
    environment:
      - POSTGRES_LOG_STATEMENT=all
      
  postgres-gig:
    environment:
      - POSTGRES_LOG_STATEMENT=all
EOF
    echo -e "${GREEN}✅ Created docker-compose.override.yml${NC}"
else
    echo -e "${YELLOW}⚠️  docker-compose.override.yml already exists${NC}"
fi

# Create helpful aliases script
echo -e "${BLUE}🔧 Creating development aliases...${NC}"
cat > dev-aliases.sh << 'EOF'
#!/bin/bash

# Freelancer Platform - Development Aliases
# Source this file to get helpful aliases: source dev-aliases.sh

# Platform management
alias platform-start='./deploy.sh start'
alias platform-stop='./deploy.sh stop'
alias platform-restart='./deploy.sh restart'
alias platform-logs='./deploy.sh logs'
alias platform-status='./deploy.sh status'
alias platform-dev='./deploy.sh dev'
alias platform-build='./deploy.sh build'
alias platform-clean='./deploy.sh clean'

# Docker shortcuts
alias dc='docker-compose'
alias dps='docker-compose ps'
alias dlogs='docker-compose logs'
alias dexec='docker-compose exec'

# Service specific logs
alias auth-logs='docker-compose logs -f auth-service'
alias gig-logs='docker-compose logs -f gig-service'
alias job-logs='docker-compose logs -f job-proposal-service'
alias workspace-logs='docker-compose logs -f workspace-service'
alias notification-logs='docker-compose logs -f notification-service'
alias payment-logs='docker-compose logs -f payment-service'
alias ai-logs='docker-compose logs -f ai-service'
alias gateway-logs='docker-compose logs -f api-gateway'

# Database connections
alias connect-auth-db='docker-compose exec postgres-auth psql -U freelancer -d freelancer_auth'
alias connect-gig-db='docker-compose exec postgres-gig psql -U freelancer -d freelancer_gig'
alias connect-job-db='docker-compose exec postgres-job psql -U freelancer -d freelancer_jobs'
alias connect-workspace-db='docker-compose exec postgres-workspace psql -U freelancer -d freelancer_workspace'
alias connect-notification-db='docker-compose exec postgres-notification psql -U freelancer -d freelancer_notifications'
alias connect-payment-db='docker-compose exec postgres-payment psql -U freelancer -d freelancer_payments'

# Redis connection
alias connect-redis='docker-compose exec redis redis-cli'

# Useful functions
platform-restart-service() {
    if [ -z "$1" ]; then
        echo "Usage: platform-restart-service <service-name>"
        echo "Example: platform-restart-service auth-service"
        return 1
    fi
    docker-compose restart "$1"
}

platform-shell() {
    if [ -z "$1" ]; then
        echo "Usage: platform-shell <service-name>"
        echo "Example: platform-shell auth-service"
        return 1
    fi
    docker-compose exec "$1" /bin/bash
}

platform-rebuild-service() {
    if [ -z "$1" ]; then
        echo "Usage: platform-rebuild-service <service-name>"
        echo "Example: platform-rebuild-service auth-service"
        return 1
    fi
    docker-compose stop "$1"
    docker-compose build "$1"
    docker-compose up -d "$1"
}

echo "🚀 Freelancer Platform development aliases loaded!"
echo "Available commands:"
echo "  platform-start, platform-stop, platform-restart"
echo "  platform-logs, platform-status, platform-dev"
echo "  auth-logs, gig-logs, job-logs, workspace-logs"
echo "  connect-auth-db, connect-gig-db, connect-redis"
echo "  platform-restart-service <service>, platform-shell <service>"
EOF

chmod +x dev-aliases.sh
echo -e "${GREEN}✅ Created dev-aliases.sh${NC}"

# Create README for developers
echo -e "${BLUE}📚 Creating developer README...${NC}"
if [ ! -f "DEVELOPMENT.md" ]; then
    cat > DEVELOPMENT.md << 'EOF'
# Freelancer Platform - Development Guide

## 🚀 Quick Start

1. **Setup Development Environment**
   ```bash
   ./setup-dev.sh
   ```

2. **Build All Services**
   ```bash
   ./deploy.sh build
   ```

3. **Start Platform in Development Mode**
   ```bash
   ./deploy.sh dev
   ```

4. **Load Development Aliases** (optional)
   ```bash
   source dev-aliases.sh
   ```

## 🛠️ Available Commands

### Platform Management
- `./deploy.sh start` - Start all services in background
- `./deploy.sh stop` - Stop all services
- `./deploy.sh restart` - Restart all services
- `./deploy.sh dev` - Start with logs (development mode)
- `./deploy.sh logs` - Show logs from all services
- `./deploy.sh status` - Show service status
- `./deploy.sh build` - Build all Docker images
- `./deploy.sh clean` - Clean up containers and images

### Development Shortcuts (after `source dev-aliases.sh`)
- `platform-start`, `platform-stop`, `platform-restart`
- `auth-logs`, `gig-logs`, `job-logs` - Service-specific logs
- `connect-auth-db`, `connect-gig-db` - Database connections
- `platform-restart-service <name>` - Restart specific service
- `platform-shell <name>` - Open shell in service container

## 🐳 Docker Services

### Application Services
- **API Gateway**: `http://localhost:8080` (Debug: 5004)
- **Auth Service**: `http://localhost:8081` (Debug: 5005)
- **Gig Service**: `http://localhost:8082` (Debug: 5006)
- **Job Proposal Service**: `http://localhost:8083` (Debug: 5007)
- **Workspace Service**: `http://localhost:8084` (Debug: 5008)
- **Notification Service**: `http://localhost:8085` (Debug: 5009)
- **Payment Service**: `http://localhost:8086` (Debug: 5010)
- **AI Service**: `http://localhost:8087` (Debug: 5011)

### Infrastructure Services
- **PostgreSQL Databases**: Multiple databases for each service
- **Redis**: `localhost:6379` (for AI service memory)
- **Kafka**: `localhost:9092` (for event streaming)
- **Zookeeper**: `localhost:2181` (Kafka dependency)

## 🔧 Development Configuration

### Environment Variables
Edit `.env` file to configure:
- Database credentials
- JWT secrets
- Stripe API keys
- Cloudinary credentials
- Service URLs

### Debug Ports
Each service has a debug port for remote debugging:
- Auth: 5005, Gig: 5006, Job: 5007, Workspace: 5008
- Notification: 5009, Payment: 5010, AI: 5011

### Hot Reload
Services are configured for hot reload in development mode.
Modify `docker-compose.override.yml` to mount source code if needed.

## 🧪 Testing

### API Testing
Test files are available in `apis/` directory:
- Use Postman, curl, or any HTTP client
- Import test collections from `apis/` folder

### Health Checks
All services include health check endpoints:
- `GET /health` - Service health status
- `GET /actuator/health` - Spring Boot actuator health

## 🗃️ Database Access

### Connect to Databases
```bash
# Auth database
docker-compose exec postgres-auth psql -U freelancer -d freelancer_auth

# Gig database (includes AI knowledge_base)
docker-compose exec postgres-gig psql -U freelancer -d freelancer_gig

# Other databases
docker-compose exec postgres-<service> psql -U freelancer -d freelancer_<service>
```

### Redis
```bash
docker-compose exec redis redis-cli
```

## 📊 Monitoring

### View Logs
```bash
# All services
./deploy.sh logs

# Specific service
docker-compose logs -f <service-name>

# Follow logs with aliases
auth-logs  # or gig-logs, job-logs, etc.
```

### Service Status
```bash
./deploy.sh status
```

## 🚨 Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   ./deploy.sh stop
   ./deploy.sh clean
   ./deploy.sh start
   ```

2. **Service Won't Start**
   ```bash
   docker-compose logs <service-name>
   ```

3. **Database Connection Issues**
   - Check if PostgreSQL containers are running
   - Verify database credentials in `.env`
   - Check service logs for connection errors

4. **Build Failures**
   ```bash
   # Clean and rebuild
   ./deploy.sh clean
   ./deploy.sh build
   ```

### Reset Everything
```bash
./deploy.sh clean  # Removes all containers and images
./deploy.sh build  # Rebuild everything
./deploy.sh start  # Start fresh
```

## 🔄 Development Workflow

1. **Make Changes** to service code
2. **Rebuild Service** (if needed):
   ```bash
   platform-rebuild-service <service-name>
   ```
3. **Test Changes** using API endpoints
4. **Check Logs** for any issues:
   ```bash
   <service>-logs
   ```
5. **Commit Changes** when ready

## 📝 Notes

- Services start in dependency order (databases first, then applications)
- Health checks ensure services are ready before marking as "healthy"
- All data is persisted in Docker volumes
- Development overrides are in `docker-compose.override.yml`
EOF
    echo -e "${GREEN}✅ Created DEVELOPMENT.md${NC}"
else
    echo -e "${YELLOW}⚠️  DEVELOPMENT.md already exists${NC}"
fi

echo
echo -e "${GREEN}🎉 Development setup completed successfully!${NC}"
echo
echo -e "${BLUE}📋 Next Steps:${NC}"
echo -e "${YELLOW}1. Update .env file with your actual credentials${NC}"
echo -e "${YELLOW}2. Run './deploy.sh build' to build all services${NC}"
echo -e "${YELLOW}3. Run './deploy.sh dev' to start in development mode${NC}"
echo -e "${YELLOW}4. Source dev-aliases.sh for helpful shortcuts: 'source dev-aliases.sh'${NC}"
echo
echo -e "${BLUE}📚 Read DEVELOPMENT.md for detailed development guide${NC}"