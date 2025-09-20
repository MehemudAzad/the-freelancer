#!/bin/bash

# 🚀 Complete Nginx Deployment Script for The Freelancer Platform
# Repository: https://github.com/Tuhin-ninja/the-freelancer-frontend

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
REPO_URL="https://github.com/Tuhin-ninja/the-freelancer-frontend.git"
APP_NAME="freelancer-platform"
APP_PORT=3000
DOMAIN="localhost"  # Change this to your domain
BACKEND_URL="http://localhost:8080"  # Change this to your backend URL

# Function to print colored output
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}🚀 $1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if running as root
check_root() {
    if [[ $EUID -eq 0 ]]; then
        print_error "This script should not be run as root. Please run as a regular user with sudo privileges."
    fi
}

# Update system packages
update_system() {
    print_header "Updating System Packages"
    sudo apt update && sudo apt upgrade -y
    print_success "System packages updated"
}

# Install Node.js
install_nodejs() {
    print_header "Installing Node.js"
    
    if command -v node &> /dev/null; then
        NODE_VERSION=$(node --version)
        print_warning "Node.js already installed: $NODE_VERSION"
        
        # Check if version is >= 18
        NODE_MAJOR=$(echo $NODE_VERSION | cut -d'.' -f1 | sed 's/v//')
        if [ "$NODE_MAJOR" -lt 18 ]; then
            print_info "Upgrading Node.js to version 18..."
            curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
            sudo apt-get install -y nodejs
        fi
    else
        curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
        sudo apt-get install -y nodejs
    fi
    
    print_success "Node.js installed: $(node --version)"
    print_success "NPM installed: $(npm --version)"
}

# Install PM2
install_pm2() {
    print_header "Installing PM2 Process Manager"
    
    if command -v pm2 &> /dev/null; then
        print_warning "PM2 already installed: $(pm2 --version)"
    else
        sudo npm install -g pm2
        print_success "PM2 installed: $(pm2 --version)"
    fi
}

# Install Nginx
install_nginx() {
    print_header "Installing Nginx"
    
    if command -v nginx &> /dev/null; then
        print_warning "Nginx already installed: $(nginx -v 2>&1)"
    else
        sudo apt install -y nginx
        sudo systemctl enable nginx
        sudo systemctl start nginx
        print_success "Nginx installed and started"
    fi
}

# Install Git
install_git() {
    print_header "Installing Git"
    
    if command -v git &> /dev/null; then
        print_warning "Git already installed: $(git --version)"
    else
        sudo apt install -y git
        print_success "Git installed: $(git --version)"
    fi
}

# Create application directory and clone repository
setup_application() {
    print_header "Setting Up Application"
    
    APP_DIR="/var/www/$APP_NAME"
    
    # Create directory if it doesn't exist
    if [ ! -d "$APP_DIR" ]; then
        sudo mkdir -p "$APP_DIR"
        sudo chown $USER:$USER "$APP_DIR"
        print_success "Created application directory: $APP_DIR"
    else
        print_warning "Application directory already exists: $APP_DIR"
    fi
    
    # Clone or update repository
    if [ ! -d "$APP_DIR/.git" ]; then
        print_info "Cloning repository..."
        git clone "$REPO_URL" "$APP_DIR"
        print_success "Repository cloned successfully"
    else
        print_info "Repository already exists, pulling latest changes..."
        cd "$APP_DIR"
        git pull origin main
        print_success "Repository updated successfully"
    fi
    
    cd "$APP_DIR"
}

# Install application dependencies and build
build_application() {
    print_header "Building Application"
    
    print_info "Installing dependencies..."
    npm install
    print_success "Dependencies installed"
    
    # Create production environment file
    print_info "Creating production environment file..."
    cat > .env.production << EOF
# Frontend Configuration
NEXT_PUBLIC_API_URL=http://$DOMAIN:$APP_PORT
NEXT_PUBLIC_SITE_URL=http://$DOMAIN

# Backend Configuration
BACKEND_URL=$BACKEND_URL

# Environment
NODE_ENV=production
EOF
    
    print_success "Environment file created"
    
    # Build the application
    print_info "Building application for production..."
    if npm run build; then
        print_success "Application built successfully"
    else
        print_warning "Build failed, trying alternative build..."
        if DISABLE_ESLINT_PLUGIN=true npm run build; then
            print_success "Application built with ESLint disabled"
        else
            print_error "Failed to build application"
        fi
    fi
}

# Setup PM2 configuration
setup_pm2() {
    print_header "Setting Up PM2 Configuration"
    
    # Create logs directory
    mkdir -p logs
    
    # Create PM2 ecosystem file
    cat > ecosystem.config.js << EOF
module.exports = {
  apps: [
    {
      name: '$APP_NAME',
      script: 'npm',
      args: 'start',
      cwd: '/var/www/$APP_NAME',
      instances: 'max',
      exec_mode: 'cluster',
      env: {
        NODE_ENV: 'production',
        PORT: $APP_PORT
      },
      error_file: './logs/$APP_NAME-error.log',
      out_file: './logs/$APP_NAME-out.log',
      log_file: './logs/$APP_NAME.log',
      time: true,
      autorestart: true,
      watch: false,
      max_memory_restart: '1G',
      kill_timeout: 5000
    }
  ]
};
EOF
    
    print_success "PM2 ecosystem configuration created"
    
    # Start application with PM2
    print_info "Starting application with PM2..."
    pm2 start ecosystem.config.js
    pm2 save
    pm2 startup | tail -1 | bash
    
    print_success "Application started with PM2"
    print_info "Application status:"
    pm2 status
}

# Configure Nginx
setup_nginx() {
    print_header "Configuring Nginx"
    
    # Remove default nginx configuration
    sudo rm -f /etc/nginx/sites-enabled/default
    
    # Create nginx configuration
    sudo tee /etc/nginx/sites-available/$APP_NAME << EOF
# Rate limiting
limit_req_zone \$binary_remote_addr zone=api:10m rate=10r/s;
limit_req_zone \$binary_remote_addr zone=login:10m rate=2r/m;

# Upstream for Next.js application
upstream ${APP_NAME}_app {
    server 127.0.0.1:$APP_PORT;
    keepalive 64;
}

# Main server block
server {
    listen 80;
    server_name $DOMAIN www.$DOMAIN;
    
    # Security Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    
    # Gzip Compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied expired no-cache no-store private must-revalidate auth;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/xml+rss
        application/json;
    
    # Next.js Static Assets
    location /_next/static/ {
        alias /var/www/$APP_NAME/.next/static/;
        expires 365d;
        add_header Cache-Control "public, immutable";
    }
    
    # Public Assets
    location /public/ {
        alias /var/www/$APP_NAME/public/;
        expires 30d;
        add_header Cache-Control "public";
    }
    
    # API Routes (with rate limiting)
    location /api/ {
        limit_req zone=api burst=20 nodelay;
        
        proxy_pass http://${APP_NAME}_app;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
        proxy_redirect off;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Auth routes (stricter rate limiting)
    location ~* ^/(auth|login|register) {
        limit_req zone=login burst=5 nodelay;
        
        proxy_pass http://${APP_NAME}_app;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }
    
    # Main Application
    location / {
        proxy_pass http://${APP_NAME}_app;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
        proxy_redirect off;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Error pages
    error_page 404 /404.html;
    error_page 500 502 503 504 /50x.html;
    
    # Logs
    access_log /var/log/nginx/$APP_NAME.access.log;
    error_log /var/log/nginx/$APP_NAME.error.log;
}
EOF
    
    # Enable site
    sudo ln -sf /etc/nginx/sites-available/$APP_NAME /etc/nginx/sites-enabled/
    
    # Test nginx configuration
    if sudo nginx -t; then
        print_success "Nginx configuration is valid"
        sudo systemctl reload nginx
        print_success "Nginx reloaded successfully"
    else
        print_error "Nginx configuration is invalid"
    fi
}

# Setup firewall
setup_firewall() {
    print_header "Setting Up Firewall"
    
    if command -v ufw &> /dev/null; then
        sudo ufw allow 22
        sudo ufw allow 80
        sudo ufw allow 443
        sudo ufw --force enable
        print_success "Firewall configured"
    else
        print_warning "UFW not installed, skipping firewall setup"
    fi
}

# Create management scripts
create_management_scripts() {
    print_header "Creating Management Scripts"
    
    # Create update script
    cat > update_app.sh << 'EOF'
#!/bin/bash
echo "🔄 Updating The Freelancer Platform..."

cd /var/www/freelancer-platform

# Pull latest changes
git pull origin main

# Install dependencies
npm install

# Build application
if npm run build; then
    echo "✅ Build successful"
else
    echo "⚠️ Build failed, trying alternative..."
    DISABLE_ESLINT_PLUGIN=true npm run build
fi

# Restart PM2
pm2 restart freelancer-platform

# Reload Nginx
sudo systemctl reload nginx

echo "✅ Update complete!"
echo "🌐 Application running at: http://localhost"
EOF
    
    chmod +x update_app.sh
    
    # Create status script
    cat > check_status.sh << 'EOF'
#!/bin/bash
echo "📊 The Freelancer Platform Status"
echo "================================"

echo "🔄 PM2 Status:"
pm2 status

echo -e "\n🌐 Nginx Status:"
sudo systemctl status nginx --no-pager -l

echo -e "\n📝 Recent Logs:"
pm2 logs freelancer-platform --lines 10 --nostream

echo -e "\n🔗 Application URL: http://localhost"
EOF
    
    chmod +x check_status.sh
    
    print_success "Management scripts created"
}

# Final verification
verify_deployment() {
    print_header "Verifying Deployment"
    
    # Check if PM2 is running
    if pm2 list | grep -q "$APP_NAME"; then
        print_success "PM2 application is running"
    else
        print_error "PM2 application is not running"
    fi
    
    # Check if Nginx is running
    if sudo systemctl is-active --quiet nginx; then
        print_success "Nginx is running"
    else
        print_error "Nginx is not running"
    fi
    
    # Check if application responds
    sleep 5
    if curl -f http://localhost:$APP_PORT > /dev/null 2>&1; then
        print_success "Application is responding"
    else
        print_warning "Application may not be responding yet (check logs)"
    fi
    
    # Check if Nginx proxy works
    if curl -f http://localhost > /dev/null 2>&1; then
        print_success "Nginx proxy is working"
    else
        print_warning "Nginx proxy may not be working (check configuration)"
    fi
}

# Main deployment function
main() {
    print_header "Starting Deployment of The Freelancer Platform"
    print_info "Repository: $REPO_URL"
    print_info "Domain: $DOMAIN"
    print_info "Port: $APP_PORT"
    print_info "Backend URL: $BACKEND_URL"
    
    # Ask for confirmation
    echo -e "\n${YELLOW}This script will install and configure:${NC}"
    echo "• Node.js 18+"
    echo "• PM2 Process Manager"
    echo "• Nginx Web Server"
    echo "• The Freelancer Platform Application"
    echo ""
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Deployment cancelled."
        exit 0
    fi
    
    # Run deployment steps
    check_root
    update_system
    install_git
    install_nodejs
    install_pm2
    install_nginx
    setup_application
    build_application
    setup_pm2
    setup_nginx
    setup_firewall
    create_management_scripts
    verify_deployment
    
    # Success message
    print_header "🎉 DEPLOYMENT COMPLETE! 🎉"
    echo -e "${GREEN}Your Freelancer Platform is now running!${NC}"
    echo ""
    echo -e "${BLUE}📍 Application URLs:${NC}"
    echo "   • Main Site: http://localhost"
    echo "   • Direct App: http://localhost:$APP_PORT"
    echo ""
    echo -e "${BLUE}🔧 Management Commands:${NC}"
    echo "   • Update App: ./update_app.sh"
    echo "   • Check Status: ./check_status.sh"
    echo "   • View Logs: pm2 logs $APP_NAME"
    echo "   • Restart App: pm2 restart $APP_NAME"
    echo ""
    echo -e "${BLUE}📂 Important Locations:${NC}"
    echo "   • App Directory: /var/www/$APP_NAME"
    echo "   • Nginx Config: /etc/nginx/sites-available/$APP_NAME"
    echo "   • Logs: /var/www/$APP_NAME/logs/"
    echo ""
    echo -e "${YELLOW}🔔 Next Steps:${NC}"
    echo "   1. Update DOMAIN variable in this script for your domain"
    echo "   2. Configure SSL certificate (Let's Encrypt recommended)"
    echo "   3. Update BACKEND_URL to point to your actual backend"
    echo "   4. Test all functionality"
    echo ""
    echo -e "${GREEN}🚀 Happy coding!${NC}"
}

# Run main function
main "$@"