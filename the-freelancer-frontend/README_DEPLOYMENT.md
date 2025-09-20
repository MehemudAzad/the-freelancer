# 🚀 Deploy The Freelancer Platform to Nginx

**Repository:** https://github.com/Tuhin-ninja/the-freelancer-frontend

## 🎯 Quick Deployment (Automated)

### Option 1: One-Command Deployment

```bash
# Download and run the automated deployment script
curl -fsSL https://raw.githubusercontent.com/Tuhin-ninja/the-freelancer-frontend/main/deploy.sh | bash
```

### Option 2: Manual Download and Run

```bash
# Clone the repository first
git clone https://github.com/Tuhin-ninja/the-freelancer-frontend.git
cd the-freelancer-frontend

# Make the script executable and run
chmod +x deploy.sh
./deploy.sh
```

---

## 🛠️ Manual Deployment Steps

If you prefer to deploy manually, follow these steps:

### 1. Update System & Install Dependencies

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Node.js 18
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install PM2 and other tools
sudo npm install -g pm2
sudo apt install -y nginx git
```

### 2. Clone and Setup Application

```bash
# Create application directory
sudo mkdir -p /var/www/freelancer-platform
sudo chown $USER:$USER /var/www/freelancer-platform

# Clone repository
git clone https://github.com/Tuhin-ninja/the-freelancer-frontend.git /var/www/freelancer-platform
cd /var/www/freelancer-platform

# Install dependencies
npm install
```

### 3. Configure Environment

```bash
# Create production environment file
cat > .env.production << EOF
NEXT_PUBLIC_API_URL=http://localhost:3000
BACKEND_URL=http://localhost:8080
NODE_ENV=production
EOF
```

### 4. Build Application

```bash
# Build for production
npm run build

# If build fails due to linting, try:
# DISABLE_ESLINT_PLUGIN=true npm run build
```

### 5. Setup PM2

```bash
# The repository already includes ecosystem.config.js
pm2 start ecosystem.config.js
pm2 save
pm2 startup
```

### 6. Configure Nginx

```bash
# Use the nginx.conf template from the repository
sudo cp nginx.conf /etc/nginx/sites-available/freelancer-platform

# Enable the site
sudo ln -s /etc/nginx/sites-available/freelancer-platform /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default

# Test and reload
sudo nginx -t
sudo systemctl reload nginx
```

### 7. Setup Firewall

```bash
sudo ufw allow 22
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable
```

---

## 🔧 Configuration Options

### Environment Variables

Edit `/var/www/freelancer-platform/.env.production`:

```bash
# Your domain
NEXT_PUBLIC_API_URL=https://yourdomain.com
NEXT_PUBLIC_SITE_URL=https://yourdomain.com

# Backend API (update to your backend server)
BACKEND_URL=https://api.yourdomain.com
# or keep as localhost if backend is on same server:
# BACKEND_URL=http://localhost:8080

NODE_ENV=production
```

### Domain Configuration

Edit the nginx configuration:
```bash
sudo nano /etc/nginx/sites-available/freelancer-platform
```

Change the `server_name` line:
```nginx
server_name yourdomain.com www.yourdomain.com;
```

---

## 🌐 SSL Certificate (Optional but Recommended)

### Using Let's Encrypt (Certbot)

```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get SSL certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Auto-renewal (add to cron)
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

---

## 📊 Management Commands

### Application Management

```bash
# Check status
pm2 status
pm2 logs freelancer-platform

# Restart application
pm2 restart freelancer-platform

# Update application
cd /var/www/freelancer-platform
git pull origin main
npm install
npm run build
pm2 restart freelancer-platform
```

### Nginx Management

```bash
# Check nginx status
sudo systemctl status nginx

# Test configuration
sudo nginx -t

# Reload nginx
sudo systemctl reload nginx

# View logs
sudo tail -f /var/log/nginx/freelancer-platform.access.log
sudo tail -f /var/log/nginx/freelancer-platform.error.log
```

---

## 🔍 Troubleshooting

### Common Issues

**1. Application won't start:**
```bash
# Check PM2 logs
pm2 logs freelancer-platform

# Check if port 3000 is available
sudo netstat -tlnp | grep :3000
```

**2. Nginx 502 Bad Gateway:**
```bash
# Ensure application is running
pm2 status

# Check nginx error logs
sudo tail -f /var/log/nginx/freelancer-platform.error.log
```

**3. Build failures:**
```bash
# Try building with ESLint disabled
DISABLE_ESLINT_PLUGIN=true npm run build

# Check Node.js version (should be 18+)
node --version
```

### Health Checks

```bash
# Test application directly
curl http://localhost:3000

# Test through nginx
curl http://localhost

# Check application response
curl -I http://localhost
```

---

## 📈 Performance Optimization

### PM2 Clustering

Edit `ecosystem.config.js`:
```javascript
{
  instances: 'max',  // Use all CPU cores
  exec_mode: 'cluster'
}
```

### Nginx Caching

Add to nginx config:
```nginx
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

---

## 🎉 Success!

After deployment, your Freelancer Platform will be available at:

- **Main Site:** http://yourdomain.com (or http://localhost)
- **Direct App:** http://yourdomain.com:3000

### Key Features Deployed:
- ✅ Next.js 15 application with Turbopack
- ✅ Environment-aware API configuration
- ✅ PM2 process management with clustering
- ✅ Nginx reverse proxy with rate limiting
- ✅ Gzip compression and static file caching
- ✅ Security headers and firewall protection

**Your freelancer platform is now live! 🚀**