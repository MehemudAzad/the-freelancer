# Proposal Milestones API - Testing Guide

## 🔒 **Authentication & Authorization**

### **Endpoint Security Status**
✅ **PROTECTED**: `/api/proposals/{proposalId}/milestones` requires authentication  
✅ **Role Required**: `FREELANCER` (only freelancers can add milestones to their proposals)  
✅ **Ownership Validation**: Only the proposal creator can add/modify milestones  
✅ **API Gateway**: Properly blocks unauthenticated requests

## 📋 **API Details**

### **Endpoint**: `POST /api/proposals/{proposalId}/milestones`

**Required Headers:**
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Authentication Flow:**
1. API Gateway validates JWT token
2. Extracts user ID, email, and role from token  
3. Forwards request with `X-User-Id`, `X-User-Email`, `X-User-Role` headers
4. Controller validates FREELANCER role
5. Service validates proposal ownership (TODO: implement ownership check)

## 🎯 **Required Data Structure**

### **ProposalMilestoneCreateDto Fields**
```json
{
  "title": "String (required, not blank)",
  "description": "String (optional)", 
  "amountCents": "BigDecimal (required, > 0)",
  "currency": "String (required, ISO-4217 format)",
  "dueDate": "LocalDate (optional, YYYY-MM-DD format)",
  "orderIndex": "Integer (optional)",
  "dod": "String (optional, Definition of Done - JSON or text)"
}
```

**Note**: `proposalId` is automatically set from URL path parameter.

## 🧪 **Testing Steps**

### **Prerequisites**

1. **Create a Freelancer Account**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "freelancer@test.com",
    "password": "password123",
    "name": "Jane Freelancer",
    "role": "FREELANCER"
  }'
```

2. **Login to Get JWT Token**
```bash
FREELANCER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "freelancer@test.com",
    "password": "password123"
  }' | jq -r '.token')

echo "Token: $FREELANCER_TOKEN"
```

3. **Create a Proposal First** (You need an existing proposal)
```bash
# First, create a job (as CLIENT), then submit a proposal (as FREELANCER)
# Replace {jobId} with an actual job ID
curl -X POST http://localhost:8080/api/proposals/my-proposals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FREELANCER_TOKEN" \
  -d '{
    "jobId": 1,
    "coverLetter": "I am interested in this project...",
    "proposedRate": 5000.00,
    "deliveryDays": 30
  }'
```

### **Step 1: Add Milestone to Proposal**
```bash
# Replace {proposalId} with your actual proposal ID (e.g., 1)
curl -X POST http://localhost:8080/api/proposals/1/milestones \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FREELANCER_TOKEN" \
  -d @test-proposal-milestone.json
```

### **Step 2: Get Proposal Milestones**
```bash
curl -X GET http://localhost:8080/api/proposals/1/milestones \
  -H "Authorization: Bearer $FREELANCER_TOKEN"
```

### **Step 3: Update Milestone**
```bash
# Replace {milestoneId} with actual milestone ID
curl -X PUT http://localhost:8080/api/proposals/1/milestones/{milestoneId} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FREELANCER_TOKEN" \
  -d '{
    "title": "Updated Backend API Development", 
    "description": "Updated milestone with additional requirements",
    "amountCents": 275000,
    "currency": "USD",
    "dueDate": "2024-02-20"
  }'
```

### **Step 4: Delete Milestone**
```bash
# Replace {milestoneId} with actual milestone ID
curl -X DELETE http://localhost:8080/api/proposals/1/milestones/{milestoneId} \
  -H "Authorization: Bearer $FREELANCER_TOKEN"
```

## ✅ **Expected Success Responses**

### **Milestone Creation (201 Created)**
```json
{
  "id": 1,
  "proposalId": 1,
  "title": "Backend API Development",
  "description": "Develop RESTful APIs with authentication...",
  "amountCents": 250000,
  "currency": "USD",
  "dueDate": "2024-02-15",
  "orderIndex": 1,
  "dod": "{\n  \"requirements\": [...]\n}",
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### **Get Milestones (200 OK)**
```json
[
  {
    "id": 1,
    "proposalId": 1,
    "title": "Backend API Development",
    "description": "Develop RESTful APIs...",
    "amountCents": 250000,
    "currency": "USD",
    "dueDate": "2024-02-15",
    "orderIndex": 1,
    "createdAt": "2024-01-15T10:30:00Z"
  }
]
```

## ❌ **Error Scenarios**

### **Missing Authentication (401)**
```bash
# Try without token
curl -X POST http://localhost:8080/api/proposals/1/milestones \
  -H "Content-Type: application/json" \
  -d @test-proposal-milestone.json

# Response:
{
  "error": "Missing or invalid Authorization header",
  "status": 401
}
```

### **Wrong Role (403)**
```bash
# Try with CLIENT token instead of FREELANCER
curl -X POST http://localhost:8080/api/proposals/1/milestones \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -d @test-proposal-milestone.json

# Response:
{
  "error": "Access denied: Only freelancers can add milestones to proposals",
  "status": 403
}
```

### **Validation Errors (400)**
```bash
# Try with invalid data
curl -X POST http://localhost:8080/api/proposals/1/milestones \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FREELANCER_TOKEN" \
  -d '{
    "title": "",
    "amountCents": -100,
    "currency": null
  }'

# Response:
{
  "error": "Validation failed",
  "details": {
    "title": "Title is required",
    "amountCents": "Amount must be greater than 0",
    "currency": "Currency is required"
  }
}
```

### **Proposal Not Found (404)**
```bash
# Try with non-existent proposal ID
curl -X POST http://localhost:8080/api/proposals/99999/milestones \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FREELANCER_TOKEN" \
  -d @test-proposal-milestone.json

# Response:
{
  "error": "Proposal not found with ID: 99999", 
  "status": 404
}
```

## 🛡️ **Security Features**

### **Current Protection**
- ✅ **JWT Token Required**: API Gateway validates all requests
- ✅ **Role-Based Access**: Only FREELANCER role can add milestones  
- ✅ **Authentication Headers**: User context forwarded to service
- ✅ **Input Validation**: DTO validation prevents malformed data

### **TODO: Enhanced Security**
- ⚠️ **Proposal Ownership**: Service should validate that authenticated user owns the proposal
- 💡 **Rate Limiting**: Consider adding rate limits for milestone creation
- 💡 **Content Validation**: Validate DoD JSON structure if provided

## 🎯 **Business Logic**

### **Milestone Purpose**
- **Proposal Structure**: Break down work into manageable chunks
- **Payment Planning**: Define payment milestones for client approval
- **Project Planning**: Establish clear deliverables and timelines
- **DoD Framework**: Define acceptance criteria for each milestone

### **Workflow Integration**
1. **Freelancer** submits proposal with milestones
2. **Client** reviews proposal and milestones
3. **If accepted** → Milestones become contract milestones
4. **Progress Tracking** → Client accepts/rejects milestone deliverables
5. **Payment Release** → Funds released upon milestone acceptance

## 📝 **JSON Template**

```json
{
  "title": "Your Milestone Title",
  "description": "Detailed description of work to be done", 
  "amountCents": 100000,
  "currency": "USD",
  "dueDate": "2024-03-15",
  "orderIndex": 1,
  "dod": "Definition of done (plain text or JSON string)"
}
```

**Ready to test proposal milestone creation with proper authentication! 🚀**

## 📄 **Updated JSON Examples**

### **Simple Example** (use `simple-proposal-milestone.json`):
```json
{
  "title": "Design Homepage UI",
  "description": "Create a modern, responsive homepage layout based on client requirements.",
  "amountCents": 50000,
  "currency": "USD", 
  "dueDate": "2025-09-20",
  "orderIndex": 1,
  "dod": "Figma design file delivered and approved by client."
}
```

### **Complex Example** (use `test-proposal-milestone.json`):
- Includes structured Definition of Done with requirements, deliverables, and acceptance criteria
- Suitable for comprehensive testing scenarios

**✅ Fixed Issues:**
- ❌ Removed `proposalId` from request body (it's in the URL path)
- ❌ Fixed database column type mismatch for `dod` field (JSONB → TEXT)
