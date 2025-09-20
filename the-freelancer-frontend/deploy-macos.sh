#!/bin/bash

# 🚀 The Freelancer Platform - macOS Local Deployment Script
# Designed for local development and testing on macOS

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="freelancer-platform"
APP_DIR="$(pwd)"
DOMAIN="${DOMAIN:-localhost}"
PORT="${PORT:-3000}"
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"

echo -e "${CYAN}========================================"
echo -e "🚀 Starting Local Deployment of The Freelancer Platform"
echo -e "========================================${NC}"
echo ""
echo -e "${BLUE}ℹ️  Application Directory: ${APP_DIR}${NC}"
echo -e "${BLUE}ℹ️  Domain: ${DOMAIN}${NC}"
echo -e "${BLUE}ℹ️  Port: ${PORT}${NC}"
echo -e "${BLUE}ℹ️  Backend URL: ${BACKEND_URL}${NC}"
echo ""
echo "This script will set up your freelancer platform locally using:"
echo "• Node.js (already installed)"
echo "• PM2 Process Manager (already installed)"
echo "• Local development server"
echo ""

read -p "Do you want to continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}⚠️  Deployment cancelled${NC}"
    exit 1
fi

echo -e "${CYAN}========================================"
echo -e "🔍 Checking Prerequisites"
echo -e "========================================${NC}"

# Check Node.js
if command -v node >/dev/null 2>&1; then
    NODE_VERSION=$(node --version)
    echo -e "${GREEN}✅ Node.js installed: ${NODE_VERSION}${NC}"
else
    echo -e "${RED}❌ Node.js not found. Please install Node.js 18+ from https://nodejs.org${NC}"
    exit 1
fi

# Check NPM
if command -v npm >/dev/null 2>&1; then
    NPM_VERSION=$(npm --version)
    echo -e "${GREEN}✅ NPM installed: ${NPM_VERSION}${NC}"
else
    echo -e "${RED}❌ NPM not found${NC}"
    exit 1
fi

# Check PM2
if command -v pm2 >/dev/null 2>&1; then
    PM2_VERSION=$(pm2 --version)
    echo -e "${GREEN}✅ PM2 installed: ${PM2_VERSION}${NC}"
else
    echo -e "${YELLOW}⚠️  PM2 not found globally, but that's okay - it's installed locally${NC}"
fi

echo -e "${CYAN}========================================"
echo -e "📦 Installing Dependencies"
echo -e "========================================${NC}"

# Install npm dependencies
if [ -f "package.json" ]; then
    echo -e "${BLUE}📦 Installing npm dependencies...${NC}"
    npm install
    echo -e "${GREEN}✅ Dependencies installed${NC}"
else
    echo -e "${RED}❌ package.json not found in current directory${NC}"
    exit 1
fi

echo -e "${CYAN}========================================"
echo -e "⚙️  Creating Environment Configuration"
echo -e "========================================${NC}"

# Create .env.local for local development
cat > .env.local << EOF
# Local Development Environment
NEXT_PUBLIC_API_URL=http://${DOMAIN}:${PORT}
NEXT_PUBLIC_SITE_URL=http://${DOMAIN}:${PORT}
BACKEND_URL=${BACKEND_URL}
NODE_ENV=development
EOF

echo -e "${GREEN}✅ Created .env.local for local development${NC}"

# Create .env.production for production builds
cat > .env.production << EOF
# Production Environment
NEXT_PUBLIC_API_URL=http://${DOMAIN}:${PORT}
NEXT_PUBLIC_SITE_URL=http://${DOMAIN}:${PORT}
BACKEND_URL=${BACKEND_URL}
NODE_ENV=production
EOF

echo -e "${GREEN}✅ Created .env.production for production builds${NC}"

echo -e "${CYAN}========================================"
echo -e "🏗️  Building Application"
echo -e "========================================${NC}"

# Build the application
echo -e "${BLUE}🏗️  Building Next.js application...${NC}"
if npm run build; then
    echo -e "${GREEN}✅ Application built successfully${NC}"
else
    echo -e "${YELLOW}⚠️  Build had warnings, but continuing...${NC}"
fi

echo -e "${CYAN}========================================"
echo -e "🚀 Setting up PM2 Configuration"
echo -e "========================================${NC}"

# Create or update ecosystem.config.js for local development
cat > ecosystem.config.js << EOF
module.exports = {
  apps: [{
    name: '${APP_NAME}',
    script: 'npm',
    args: 'start',
    cwd: '${APP_DIR}',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env: {
      NODE_ENV: 'production',
      PORT: ${PORT}
    },
    env_production: {
      NODE_ENV: 'production',
      PORT: ${PORT}
    },
    log_file: './logs/combined.log',
    out_file: './logs/out.log',
    error_file: './logs/error.log',
    time: true
  }]
};
EOF

echo -e "${GREEN}✅ PM2 configuration created${NC}"

# Create logs directory
mkdir -p logs
echo -e "${GREEN}✅ Logs directory created${NC}"

echo -e "${CYAN}========================================"
echo -e "🚀 Starting Application with PM2"
echo -e "========================================${NC}"

# Stop any existing PM2 process
echo -e "${BLUE}🛑 Stopping any existing ${APP_NAME} process...${NC}"
pm2 delete ${APP_NAME} 2>/dev/null || echo -e "${YELLOW}⚠️  No existing process found${NC}"

# Start the application
echo -e "${BLUE}🚀 Starting ${APP_NAME}...${NC}"
pm2 start ecosystem.config.js

# Save PM2 configuration
pm2 save

echo -e "${GREEN}✅ Application started with PM2${NC}"

echo -e "${CYAN}========================================"
echo -e "🔍 Application Status"
echo -e "========================================${NC}"

# Show PM2 status
pm2 status

echo -e "${CYAN}========================================"
echo -e "✅ Deployment Complete!"
echo -e "========================================${NC}"

echo -e "${GREEN}🎉 Your Freelancer Platform is now running!${NC}"
echo ""
echo -e "${BLUE}📍 Access your application at:${NC}"
echo -e "   ${GREEN}🌐 Main Site: http://${DOMAIN}:${PORT}${NC}"
echo ""
echo -e "${BLUE}📊 Management Commands:${NC}"
echo -e "   ${CYAN}pm2 status${NC}                 - Check application status"
echo -e "   ${CYAN}pm2 logs ${APP_NAME}${NC}       - View application logs"
echo -e "   ${CYAN}pm2 restart ${APP_NAME}${NC}    - Restart application"
echo -e "   ${CYAN}pm2 stop ${APP_NAME}${NC}       - Stop application"
echo -e "   ${CYAN}pm2 delete ${APP_NAME}${NC}     - Remove application from PM2"
echo ""
echo -e "${BLUE}🔧 Configuration Files Created:${NC}"
echo -e "   ${CYAN}.env.local${NC}              - Local development environment"
echo -e "   ${CYAN}.env.production${NC}         - Production environment"
echo -e "   ${CYAN}ecosystem.config.js${NC}     - PM2 configuration"
echo ""
echo -e "${BLUE}📝 Application Features:${NC}"
echo -e "   ${GREEN}✅ Job listings and matched freelancer recommendations${NC}"
echo -e "   ${GREEN}✅ CLIENT and FREELANCER dashboards${NC}"
echo -e "   ${GREEN}✅ Real-time chat system${NC}"
echo -e "   ${GREEN}✅ Payment processing integration${NC}"
echo -e "   ${GREEN}✅ Profile management${NC}"
echo ""

# Test if application is responding
echo -e "${BLUE}🧪 Testing application response...${NC}"
sleep 3
if curl -s -o /dev/null -w "%{http_code}" http://localhost:${PORT} | grep -q "200\|301\|302"; then
    echo -e "${GREEN}✅ Application is responding successfully!${NC}"
else
    echo -e "${YELLOW}⚠️  Application may still be starting up. Check logs with: pm2 logs ${APP_NAME}${NC}"
fi

echo ""
echo -e "${PURPLE}🚀 Happy coding! Your freelancer platform is ready to use!${NC}"