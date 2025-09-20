# 🚀 Production Deployment Guide: The Freelancer Platform

## Complete Step-by-Step Nginx Deployment

### Prerequisites
- Ubuntu/CentOS/Debian server with root access
- Domain name (e.g., `yourfreelancerplatform.com`)
- Backend API server running (assumed on port 8080)

---

## 🔧 Step 1: Server Setup & Dependencies

### 1.1 Update System
```bash
sudo apt update && sudo apt upgrade -y
```

### 1.2 Install Node.js (v18+)
```bash
# Install Node.js via NodeSource
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Verify installation
node --version
npm --version
```

### 1.3 Install PM2 (Process Manager)
```bash
sudo npm install -g pm2
```

### 1.4 Install Nginx
```bash
sudo apt install -y nginx
```

### 1.5 Install Git
```bash
sudo apt install -y git
```

---

## 📁 Step 2: Clone & Setup Application

### 2.1 Create Application Directory
```bash
sudo mkdir -p /var/www/freelancer-platform
sudo chown $USER:$USER /var/www/freelancer-platform
cd /var/www/freelancer-platform
```

### 2.2 Clone Repository
```bash
git clone https://github.com/Tuhin-ninja/the-freelancer-frontend.git .
```

### 2.3 Install Dependencies
```bash
npm install
```

### 2.4 Create Production Environment File
```bash
cat > .env.production << EOF
# Frontend Configuration
NEXT_PUBLIC_API_URL=https://yourfreelancerplatform.com
NEXT_PUBLIC_SITE_URL=https://yourfreelancerplatform.com

# Backend Configuration
BACKEND_URL=http://localhost:8080
# Or if backend is on different server:
# BACKEND_URL=https://api.yourfreelancerplatform.com

# Environment
NODE_ENV=production
EOF
```

### 2.5 Build Application (Skip Linting)
```bash
# Update package.json to skip linting in production
npm run build:prod || DISABLE_ESLINT_PLUGIN=true npm run build
```

---

## 🔄 Step 3: PM2 Configuration

### 3.1 Create PM2 Ecosystem File
```bash
cat > ecosystem.config.js << EOF
module.exports = {
  apps: [
    {
      name: 'freelancer-platform',
      script: 'npm',
      args: 'start',
      cwd: '/var/www/freelancer-platform',
      instances: 'max',
      exec_mode: 'cluster',
      env: {
        NODE_ENV: 'production',
        PORT: 3000
      },
      error_file: '/var/log/pm2/freelancer-platform-error.log',
      out_file: '/var/log/pm2/freelancer-platform-out.log',
      log_file: '/var/log/pm2/freelancer-platform.log',
      time: true,
      autorestart: true,
      watch: false,
      max_memory_restart: '1G'
    }
  ]
};
EOF
```

### 3.2 Create Log Directory
```bash
sudo mkdir -p /var/log/pm2
sudo chown $USER:$USER /var/log/pm2
```

### 3.3 Start Application with PM2
```bash
pm2 start ecosystem.config.js
pm2 save
pm2 startup
```

---

## 🌐 Step 4: Nginx Configuration

### 4.1 Remove Default Nginx Config
```bash
sudo rm /etc/nginx/sites-enabled/default
```

### 4.2 Create Nginx Configuration
```bash
sudo tee /etc/nginx/sites-available/freelancer-platform << EOF
# Rate limiting
limit_req_zone \$binary_remote_addr zone=api:10m rate=10r/s;
limit_req_zone \$binary_remote_addr zone=login:10m rate=2r/m;

# Upstream for Next.js application
upstream freelancer_app {
    server 127.0.0.1:3000;
    keepalive 64;
}

# HTTP Server (Redirect to HTTPS)
server {
    listen 80;
    server_name yourfreelancerplatform.com www.yourfreelancerplatform.com;
    
    # Redirect all HTTP requests to HTTPS
    return 301 https://\$server_name\$request_uri;
}

# HTTPS Server
server {
    listen 443 ssl http2;
    server_name yourfreelancerplatform.com www.yourfreelancerplatform.com;
    
    # SSL Configuration (You'll need to add SSL certificates)
    # ssl_certificate /etc/ssl/certs/yourfreelancerplatform.com.crt;
    # ssl_certificate_key /etc/ssl/private/yourfreelancerplatform.com.key;
    
    # For now, remove SSL lines and use HTTP only for testing
    listen 80;
    # Comment out the SSL lines above for initial testing
    
    # Security Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
    
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
        alias /var/www/freelancer-platform/.next/static/;
        expires 365d;
        add_header Cache-Control "public, immutable";
    }
    
    # Public Assets
    location /public/ {
        alias /var/www/freelancer-platform/public/;
        expires 30d;
        add_header Cache-Control "public";
    }
    
    # API Routes (with rate limiting)
    location /api/ {
        limit_req zone=api burst=20 nodelay;
        
        proxy_pass http://freelancer_app;
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
        
        proxy_pass http://freelancer_app;
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
        proxy_pass http://freelancer_app;
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
    access_log /var/log/nginx/freelancer-platform.access.log;
    error_log /var/log/nginx/freelancer-platform.error.log;
}
EOF
```

### 4.3 Enable Site & Test Configuration
```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/freelancer-platform /etc/nginx/sites-enabled/

# Test nginx configuration
sudo nginx -t

# If test passes, reload nginx
sudo systemctl reload nginx
```

---

## 🔐 Step 5: SSL Certificate (Optional but Recommended)

### 5.1 Install Certbot
```bash
sudo apt install -y certbot python3-certbot-nginx
```

### 5.2 Obtain SSL Certificate
```bash
sudo certbot --nginx -d yourfreelancerplatform.com -d www.yourfreelancerplatform.com
```

### 5.3 Auto-renewal Setup
```bash
sudo crontab -e
# Add this line:
0 12 * * * /usr/bin/certbot renew --quiet
```

---

## 🔄 Step 6: System Services & Firewall

### 6.1 Enable Services
```bash
sudo systemctl enable nginx
sudo systemctl enable pm2-$USER
```

### 6.2 Configure Firewall
```bash
sudo ufw allow 22
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable
```

---

## 📊 Step 7: Monitoring & Maintenance

### 7.1 Check Application Status
```bash
# PM2 status
pm2 status
pm2 logs freelancer-platform

# Nginx status
sudo systemctl status nginx
sudo tail -f /var/log/nginx/freelancer-platform.access.log
```

### 7.2 Useful PM2 Commands
```bash
# Restart application
pm2 restart freelancer-platform

# View logs
pm2 logs freelancer-platform --lines 100

# Monitor resources
pm2 monit

# Reload application (zero downtime)
pm2 reload freelancer-platform
```

### 7.3 Update Deployment Script
```bash
cat > deploy.sh << EOF
#!/bin/bash
echo "🚀 Deploying Freelancer Platform..."

# Pull latest changes
git pull origin main

# Install dependencies
npm install

# Build application
DISABLE_ESLINT_PLUGIN=true npm run build

# Restart PM2
pm2 restart freelancer-platform

# Reload Nginx
sudo systemctl reload nginx

echo "✅ Deployment complete!"
EOF

chmod +x deploy.sh
```

---

## 🔧 Step 8: Troubleshooting

### 8.1 Common Issues

**Application won't start:**
```bash
# Check PM2 logs
pm2 logs freelancer-platform

# Check if port 3000 is available
sudo netstat -tlnp | grep :3000
```

**Nginx 502 Bad Gateway:**
```bash
# Check if Next.js app is running
pm2 status

# Check nginx error logs
sudo tail -f /var/log/nginx/freelancer-platform.error.log
```

**Environment Variables:**
```bash
# Check if .env.production is loaded
pm2 show freelancer-platform
```

### 8.2 Performance Optimization
```bash
# Enable nginx status page
echo "
location /nginx_status {
    stub_status on;
    access_log off;
    allow 127.0.0.1;
    deny all;
}" | sudo tee -a /etc/nginx/sites-available/freelancer-platform
```

---

## ✅ Step 9: Final Verification

1. **Test Application:** `http://yourfreelancerplatform.com`
2. **Check API Endpoints:** `http://yourfreelancerplatform.com/api/health`
3. **Monitor Logs:** `pm2 logs freelancer-platform --lines 50`
4. **Verify SSL:** `https://yourfreelancerplatform.com` (if configured)

---

## 📝 Quick Commands Summary

```bash
# Deploy updates
./deploy.sh

# Check status
pm2 status && sudo systemctl status nginx

# View logs
pm2 logs freelancer-platform && sudo tail -f /var/log/nginx/freelancer-platform.access.log

# Restart services
pm2 restart freelancer-platform && sudo systemctl restart nginx
```

---

Your Freelancer Platform should now be successfully deployed and running on Nginx! 🎉

**Next Steps:**
1. Configure your domain DNS to point to your server
2. Set up SSL certificates for HTTPS
3. Configure monitoring and backups
4. Set up your backend API server
5. Test all functionality thoroughly