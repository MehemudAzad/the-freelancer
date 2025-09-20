#!/bin/bash

# Freelancer Platform - Build All Services Script
# This script builds all Docker images for the microservices platform

set -e  # Exit on any error

echo "🚀 Building Freelancer Platform Docker Images"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Services to build
SERVICES=(
    "auth-service"
    "gig-service" 
    "job-proposal-service"
    "workspace-service"
    "notification-service"
    "payment-service"
    "ai-service"
    "api-gateway"
)

# Function to build a service
build_service() {
    local service=$1
    echo -e "${BLUE}📦 Building $service...${NC}"
    
    if [ -d "./$service" ]; then
        cd "./$service"
        
        # Check if Dockerfile exists
        if [ -f "Dockerfile" ]; then
            # Build Maven project first
            echo -e "${YELLOW}  🔨 Building Maven project...${NC}"
            if ./mvnw clean package -DskipTests -q; then
                echo -e "${GREEN}  ✅ Maven build successful${NC}"
            else
                echo -e "${RED}  ❌ Maven build failed for $service${NC}"
                cd ..
                return 1
            fi
            
            # Build Docker image
            echo -e "${YELLOW}  🐳 Building Docker image...${NC}"
            if docker build -t "freelancer/$service:latest" . --quiet; then
                echo -e "${GREEN}  ✅ Docker image built successfully${NC}"
            else
                echo -e "${RED}  ❌ Docker build failed for $service${NC}"
                cd ..
                return 1
            fi
        else
            echo -e "${RED}  ❌ Dockerfile not found in $service${NC}"
            cd ..
            return 1
        fi
        
        cd ..
        return 0
    else
        echo -e "${RED}  ❌ Directory $service not found${NC}"
        return 1
    fi
}

# Build all services
failed_services=()
successful_services=()

for service in "${SERVICES[@]}"; do
    if build_service "$service"; then
        successful_services+=("$service")
    else
        failed_services+=("$service")
    fi
    echo
done

# Summary
echo "🏁 Build Summary"
echo "==============="

if [ ${#successful_services[@]} -gt 0 ]; then
    echo -e "${GREEN}✅ Successfully built (${#successful_services[@]}):${NC}"
    for service in "${successful_services[@]}"; do
        echo -e "${GREEN}  - $service${NC}"
    done
fi

if [ ${#failed_services[@]} -gt 0 ]; then
    echo -e "${RED}❌ Failed to build (${#failed_services[@]}):${NC}"
    for service in "${failed_services[@]}"; do
        echo -e "${RED}  - $service${NC}"
    done
    echo
    echo -e "${RED}⚠️  Some services failed to build. Please check the logs above.${NC}"
    exit 1
else
    echo
    echo -e "${GREEN}🎉 All services built successfully!${NC}"
    echo -e "${BLUE}💡 You can now run 'docker-compose up -d' to start the platform${NC}"
fi

# Show built images
echo
echo "📋 Built Docker Images:"
echo "======================"
docker images | grep "freelancer/" | head -10