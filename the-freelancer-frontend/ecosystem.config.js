module.exports = {
  apps: [{
    name: 'freelancer-platform',
    script: 'npm',
    args: 'start',
    cwd: '/Users/khalidhasantuhin/Library/CloudStorage/OneDrive-BUET/3-1/javafest/javafest/Ultimate/the-freelancer-frontend',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env: {
      NODE_ENV: 'production',
      PORT: 3000
    },
    env_production: {
      NODE_ENV: 'production',
      PORT: 3000
    },
    log_file: './logs/combined.log',
    out_file: './logs/out.log',
    error_file: './logs/error.log',
    time: true
  }]
};
