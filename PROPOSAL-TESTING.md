# Proposal API Testing Guide

## 🎯 **Proposal Endpoint Overview**

### **Protected Endpoints (Require FREELANCER Authentication)**
- `POST /api/proposals/my-proposals` - Submit new proposal
- `GET /api/proposals/my-proposals` - Get authenticated freelancer's proposals
- `PUT /api/proposals/my-proposals/{proposalId}` - Update own proposal
- `DELETE /api/proposals/my-proposals/{proposalId}` - Withdraw own proposal

### **Public/Shared Endpoints**
- `GET /api/proposals/{proposalId}` - View proposal details (accessible to job owner + proposal owner)

## 📋 **Required Data Structure**

### **ProposalCreateDto Fields**
```json
{
  "jobId": Long (required) - ID of the job to propose for,
  "coverLetter": String (required) - Detailed proposal cover letter,
  "proposedRate": BigDecimal (required) - Freelancer's rate in dollars (> 0),
  "deliveryDays": Integer (required) - Estimated delivery time (≥ 1),
  "portfolioLinks": String (optional) - Comma-separated portfolio URLs,
  "additionalNotes": String (optional) - Extra information for client
}
```

### **Authentication Requirements**
- **Role**: Must be `FREELANCER` 
- **Headers**: `X-User-Id`, `X-User-Role` set by API Gateway
- **JWT Token**: Valid Bearer token required

## 🧪 **Testing Steps**

### **Step 1: Create a Freelancer Account**
```bash
# Register as freelancer
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "freelancer@test.com",
    "password": "password123",
    "name": "Jane Developer", 
    "role": "FREELANCER"
  }'
```

### **Step 2: Login to Get JWT Token**
```bash
# Login and save token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "freelancer@test.com",
    "password": "password123"
  }' | jq -r '.token')

echo "Token: $TOKEN"
```

### **Step 3: Create a Job (Client Required)**
```bash
# First register/login as CLIENT to create a job
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@test.com",
    "password": "password123", 
    "name": "Test Client",
    "role": "CLIENT"
  }'

# Login as client
CLIENT_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "client@test.com",
    "password": "password123"
  }' | jq -r '.token')

# Create job using test-job.json
curl -X POST http://localhost:8080/api/jobs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d @test-job.json
```

### **Step 4: Submit Proposal (Freelancer)**
```bash
# Submit proposal using test-proposal.json
curl -X POST http://localhost:8080/api/proposals/my-proposals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d @test-proposal.json
```

### **Step 5: Get My Proposals**
```bash
# Get all my proposals
curl -X GET http://localhost:8080/api/proposals/my-proposals \
  -H "Authorization: Bearer $TOKEN"

# Get proposals with specific status
curl -X GET "http://localhost:8080/api/proposals/my-proposals?status=SUBMITTED" \
  -H "Authorization: Bearer $TOKEN"
```

### **Step 6: Update Proposal**
```bash
# Update proposal (replace {proposalId} with actual ID)
curl -X PUT http://localhost:8080/api/proposals/my-proposals/{proposalId} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "coverLetter": "Updated cover letter with additional details...",
    "proposedRate": 6500.00,
    "deliveryDays": 40,
    "portfolioLinks": "https://updated-portfolio.com",
    "additionalNotes": "Updated with more competitive pricing"
  }'
```

### **Step 7: Withdraw Proposal**
```bash
# Withdraw proposal (replace {proposalId} with actual ID)
curl -X DELETE http://localhost:8080/api/proposals/my-proposals/{proposalId} \
  -H "Authorization: Bearer $TOKEN"
```

## ✅ **Expected Success Responses**

### **Proposal Creation (201 Created)**
```json
{
  "id": 1,
  "jobId": 1,
  "freelancerId": 2,
  "cover": "Dear Client, I am excited to submit...",
  "totalCents": 600000,
  "currency": "USD", 
  "deliveryDays": 45,
  "status": "SUBMITTED",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### **Get My Proposals (200 OK)**
```json
[
  {
    "id": 1,
    "jobId": 1,
    "jobTitle": "Build a Modern E-commerce Platform",
    "cover": "Dear Client, I am excited to submit...",
    "proposedRate": 6000.00,
    "deliveryDays": 45,
    "status": "SUBMITTED",
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
]
```

## ❌ **Error Scenarios**

### **Missing Authentication (401)**
```bash
# Try without token
curl -X POST http://localhost:8080/api/proposals/my-proposals \
  -H "Content-Type: application/json" \
  -d @test-proposal.json

# Response:
{
  "error": "Missing or invalid Authorization header",
  "status": 401
}
```

### **Wrong Role (403)**
```bash
# Try with CLIENT token instead of FREELANCER
curl -X POST http://localhost:8080/api/proposals/my-proposals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d @test-proposal.json

# Response:
{
  "error": "Access denied: Only freelancers can submit proposals",
  "status": 403  
}
```

### **Validation Errors (400)**
```bash
# Try with invalid data
curl -X POST http://localhost:8080/api/proposals/my-proposals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "jobId": null,
    "coverLetter": "",
    "proposedRate": -100,
    "deliveryDays": 0
  }'

# Response:
{
  "error": "Validation failed",
  "details": {
    "jobId": "Job ID is required",
    "coverLetter": "Cover letter is required",
    "proposedRate": "Proposed rate must be greater than 0",
    "deliveryDays": "Delivery days must be at least 1"
  }
}
```

### **Job Not Found (404)**
```bash
# Try with non-existent job ID
curl -X POST http://localhost:8080/api/proposals/my-proposals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "jobId": 99999,
    "coverLetter": "Test proposal",
    "proposedRate": 1000.00,
    "deliveryDays": 30
  }'

# Response:
{
  "error": "Job not found with ID: 99999",
  "status": 404
}
```

## 🔍 **API Gateway Verification**

The `/api/proposals/my-proposals` endpoint is correctly configured as **protected**:
- ✅ **Not** in `publicEndpoints` list
- ✅ Requires valid JWT token  
- ✅ Validates user role (FREELANCER only)
- ✅ Forwards user context headers to microservice

## 📝 **Notes**

1. **Job ID Dependency**: You need an existing job to create a proposal for
2. **Role Authorization**: Only users with `FREELANCER` role can submit proposals  
3. **Rate Conversion**: The service converts `proposedRate` (decimal) to `totalCents` (integer) automatically
4. **Status Tracking**: Proposals start with `SUBMITTED` status
5. **Freelancer Context**: The `freelancerId` is automatically set from authenticated user headers

**Ready to test proposal submission! 🚀**
