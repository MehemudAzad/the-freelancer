# API Testing Guide

## Prerequisites
Before testing, make sure to start all services:

1. **Start Individual Services** (if not using docker-compose):
   ```bash
   # Terminal 1 - Auth Service
   cd auth-service && ./mvnw spring-boot:run

   # Terminal 2 - Job Proposal Service  
   cd job-proposal-service && ./mvnw spring-boot:run

   # Terminal 3 - Gig Service
   cd gig-service && ./mvnw spring-boot:run

   # Terminal 4 - API Gateway
   cd api-gateway && ./mvnw spring-boot:run
   ```

2. **Or use Docker Compose** (if uncommented):
   ```bash
   docker-compose up -d
   ```

## Service Ports
- **API Gateway**: 8080 (main entry point)
- **Auth Service**: 8081
- **Job Proposal Service**: 8082  
- **Gig Service**: 8083
- **Payment Service**: 8084

## Testing Job APIs

### 1. Create a User Account (Required for protected endpoints)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@test.com",
    "password": "password123",
    "name": "Test Client",
    "role": "CLIENT"
  }'
```

### 2. Login to Get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@test.com", 
    "password": "password123"
  }'
```

Save the JWT token from the response for authenticated requests.

### 3. Test Job Creation (Requires Authentication)
```bash
curl -X POST http://localhost:8080/api/jobs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d @test-job.json
```

### 4. Test Public Job Viewing (No Authentication Required)
```bash
# View specific job (replace {id} with actual job ID from creation response)
curl -X GET http://localhost:8080/api/jobs/{id}

# Search jobs publicly  
curl -X GET "http://localhost:8080/api/jobs/search?title=ecommerce&stack=React"
```

### 5. Test Protected Job Operations (Require Authentication)
```bash
# Get user's own jobs
curl -X GET http://localhost:8080/api/my-jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# Update job (only job owner)
curl -X PUT http://localhost:8080/api/jobs/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "title": "Updated E-commerce Platform",
    "description": "Updated requirements...",
    "stack": ["React", "Node.js", "PostgreSQL", "Stripe"],
    "budgetType": "FIXED",
    "minBudgetCents": 600000,
    "maxBudgetCents": 800000,
    "currency": "USD"
  }'
```

## Expected Responses

### Successful Job Creation (201 Created):
```json
{
  "id": 1,
  "title": "Build a Modern E-commerce Platform",
  "description": "We need a full-stack e-commerce platform...",
  "clientId": 1,
  "stack": ["React", "Node.js", "PostgreSQL", "Stripe", "Express", "JavaScript", "HTML", "CSS"],
  "budgetType": "FIXED",
  "minBudgetCents": 500000,
  "maxBudgetCents": 750000,
  "currency": "USD",
  "status": "OPEN",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### Public Job View (200 OK):
```json
{
  "id": 1,
  "title": "Build a Modern E-commerce Platform",
  "description": "We need a full-stack e-commerce platform...",
  "stack": ["React", "Node.js", "PostgreSQL", "Stripe", "Express", "JavaScript", "HTML", "CSS"],
  "budgetType": "FIXED",
  "minBudgetCents": 500000,
  "maxBudgetCents": 750000,
  "currency": "USD",
  "status": "OPEN",
  "createdAt": "2024-01-15T10:30:00Z"
  // Note: clientId is hidden for public viewing
}
```

## Error Scenarios

### Missing Authentication (401):
```json
{
  "error": "Missing or invalid Authorization header",
  "status": 401
}
```

### Invalid Token (401):
```json
{
  "error": "Invalid or expired token",
  "status": 401
}
```

### Validation Errors (400):
```json
{
  "error": "Validation failed",
  "details": {
    "title": "Title is required",
    "minBudgetCents": "Budget must be positive"
  }
}
```

## Gateway Configuration Changes Applied

The API Gateway has been updated to allow public access to:
- ✅ `GET /api/jobs/{id}` - View individual job details
- ✅ `GET /api/jobs/search` - Search jobs publicly
- ✅ All existing public endpoints (auth, gig search, swagger docs)

Protected endpoints still require authentication:
- `POST /api/jobs` - Create job (requires CLIENT role)
- `PUT /api/jobs/{id}` - Update job (requires ownership)
- `DELETE /api/jobs/{id}` - Delete job (requires ownership)
- `GET /api/my-jobs` - Get user's own jobs
