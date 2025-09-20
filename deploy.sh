#!/bin/bash

# Freelancer Platform - Deploy Script
# This script manages the deployment of the entire microservices platform

set -e

echo "🚀 Freelancer Platform Deployment Manager"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Function to show usage
show_usage() {
    echo "Usage: $0 [command] [options]"
    echo
    echo "Commands:"
    echo "  start       Start all services"
    echo "  stop        Stop all services"
    echo "  restart     Restart all services"
    echo "  build       Build all Docker images"
    echo "  logs        Show logs for all services"
    echo "  status      Show status of all services"
    echo "  clean       Clean up containers and images"
    echo "  dev         Start in development mode with logs"
    echo
    echo "Options:"
    echo "  -h, --help  Show this help message"
    echo
    echo "Examples:"
    echo "  $0 start                # Start all services in background"
    echo "  $0 dev                  # Start in development mode with logs"
    echo "  $0 logs                 # Show logs from all services"
    echo "  $0 clean               # Clean up everything"
}

# Function to check if docker-compose is available
check_docker_compose() {
    if command -v docker-compose &> /dev/null; then
        DOCKER_COMPOSE="docker-compose"
    elif command -v docker &> /dev/null && docker compose version &> /dev/null; then
        DOCKER_COMPOSE="docker compose"
    else
        echo -e "${RED}❌ Docker Compose not found. Please install Docker and Docker Compose.${NC}"
        exit 1
    fi
}

# Function to wait for services to be healthy
wait_for_services() {
    echo -e "${YELLOW}⏳ Waiting for services to start...${NC}"
    
    # Wait up to 5 minutes for services to be healthy
    local timeout=300
    local elapsed=0
    local interval=10
    
    while [ $elapsed -lt $timeout ]; do
        local unhealthy=$($DOCKER_COMPOSE ps | grep -E "(starting|unhealthy)" | wc -l)
        
        if [ $unhealthy -eq 0 ]; then
            echo -e "${GREEN}✅ All services are healthy!${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}⏳ Waiting for $unhealthy service(s) to start... (${elapsed}s/${timeout}s)${NC}"
        sleep $interval
        elapsed=$((elapsed + interval))
    done
    
    echo -e "${RED}⚠️  Timeout waiting for services. Some services may still be starting.${NC}"
    return 1
}

# Function to start services
start_services() {
    echo -e "${BLUE}🚀 Starting Freelancer Platform...${NC}"
    
    # Start services in detached mode
    $DOCKER_COMPOSE up -d
    
    # Wait for services to be healthy
    wait_for_services
    
    echo -e "${GREEN}✅ Platform started successfully!${NC}"
    echo
    show_service_status
    echo
    echo -e "${BLUE}🌐 Access Points:${NC}"
    echo -e "${BLUE}  API Gateway: http://localhost:8080${NC}"
    echo -e "${BLUE}  Auth Service: http://localhost:8081${NC}"
    echo -e "${BLUE}  Gig Service: http://localhost:8082${NC}"
    echo -e "${BLUE}  Job Proposal Service: http://localhost:8083${NC}"
    echo -e "${BLUE}  Workspace Service: http://localhost:8084${NC}"
    echo
    echo -e "${YELLOW}💡 Use '$0 logs' to see service logs${NC}"
    echo -e "${YELLOW}💡 Use '$0 status' to check service health${NC}"
}

# Function to stop services
stop_services() {
    echo -e "${YELLOW}🛑 Stopping Freelancer Platform...${NC}"
    $DOCKER_COMPOSE down
    echo -e "${GREEN}✅ Platform stopped successfully!${NC}"
}

# Function to restart services
restart_services() {
    echo -e "${YELLOW}🔄 Restarting Freelancer Platform...${NC}"
    stop_services
    echo
    start_services
}

# Function to build services
build_services() {
    echo -e "${BLUE}🔨 Building all services...${NC}"
    if [ -f "./build-all.sh" ]; then
        chmod +x ./build-all.sh
        ./build-all.sh
    else
        echo -e "${RED}❌ build-all.sh not found${NC}"
        exit 1
    fi
}

# Function to show logs
show_logs() {
    echo -e "${BLUE}📋 Showing service logs...${NC}"
    echo -e "${YELLOW}💡 Press Ctrl+C to exit logs${NC}"
    echo
    $DOCKER_COMPOSE logs -f --tail=100
}

# Function to show service status
show_service_status() {
    echo -e "${BLUE}📊 Service Status:${NC}"
    echo "=================="
    $DOCKER_COMPOSE ps
}

# Function to start in development mode
dev_mode() {
    echo -e "${BLUE}🚀 Starting Freelancer Platform in Development Mode...${NC}"
    echo -e "${YELLOW}💡 Press Ctrl+C to stop all services${NC}"
    echo
    
    # Start services and show logs
    $DOCKER_COMPOSE up
}

# Function to clean up
cleanup() {
    echo -e "${YELLOW}🧹 Cleaning up Freelancer Platform...${NC}"
    
    # Stop and remove containers
    $DOCKER_COMPOSE down -v --remove-orphans
    
    # Remove images
    echo -e "${YELLOW}🗑️  Removing Docker images...${NC}"
    docker images | grep "freelancer/" | awk '{print $3}' | xargs -r docker rmi -f
    
    # Remove unused volumes and networks
    echo -e "${YELLOW}🗑️  Cleaning up unused Docker resources...${NC}"
    docker system prune -f
    
    echo -e "${GREEN}✅ Cleanup completed!${NC}"
}

# Check prerequisites
check_docker_compose

# Parse command line arguments
case "${1:-}" in
    "start")
        start_services
        ;;
    "stop")
        stop_services
        ;;
    "restart")
        restart_services
        ;;
    "build")
        build_services
        ;;
    "logs")
        show_logs
        ;;
    "status")
        show_service_status
        ;;
    "dev")
        dev_mode
        ;;
    "clean")
        cleanup
        ;;
    "-h"|"--help"|"help")
        show_usage
        ;;
    "")
        echo -e "${RED}❌ No command specified.${NC}"
        echo
        show_usage
        exit 1
        ;;
    *)
        echo -e "${RED}❌ Unknown command: $1${NC}"
        echo
        show_usage
        exit 1
        ;;
esac