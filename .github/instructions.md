# The Freelancer Platform - Development Instructions

## 📋 **Feature Implementation Tracking**

**IMPORTANT**: Always reference and update the `features.md` file when implementing any API endpoints or completing tasks.

### **When completing a task:**
1. Mark the API as completed in `features.md` by changing `[ ]` to `[x]`
2. Add the ✅ **COMPLETED** status next to the description
3. Update the implementation progress counters at the top
4. Update the service health status table
5. Commit changes with descriptive messages

### **Example of marking completion:**
```markdown
# Before
- [ ] `POST /api/auth/login` - User login (generate JWT)

# After  
- [x] `POST /api/auth/login` - User login (generate JWT) ✅ **COMPLETED**
```

---

## 🏗️ **Project Architecture**

### **Microservices Structure**
```
the-freelancer/
├── auth-service/          (Port: 8081) - Authentication & User Management
├── gig-service/           (Port: 8082) - Profiles & Service Offerings  
├── job-proposal-service/  (Port: 8083) - Jobs & Proposals
├── contract-service/      (Port: 8084) - Contracts & Milestones [Future]
├── payment-service/       (Port: 8085) - Escrow & Payments [Future]
├── features.md           # 📊 FEATURE TRACKING - UPDATE THIS!
├── docker-compose.yml    # Infrastructure (Kafka, PostgreSQL)
└── README.md
```

### **Technology Stack**
- **Backend**: Spring Boot 3.5.5, Java 21
- **Database**: PostgreSQL 16 (separate DB per service)
- **Messaging**: Apache Kafka + Zookeeper
- **Security**: Spring Security + JWT
- **Build**: Maven
- **Containerization**: Docker & Docker Compose

---

## 🔄 **Event-Driven Architecture**

### **Current Event Flow**
```
User Registration:
Auth Service → UserCreatedEvent → Kafka → Gig Service → Profile Created ✅

Future Events:
Job Posted → JobPostedEvent → Notification Service
Proposal Accepted → ProposalAcceptedEvent → Contract Service  
Milestone Accepted → MilestoneAcceptedEvent → Payment Service
```

### **Kafka Topics**
- `user-events` - User lifecycle events
- `job-events` - Job posting and updates [Future]
- `proposal-events` - Proposal submissions and status [Future]
- `contract-events` - Contract and milestone events [Future]

---

## 📂 **Service Implementation Guidelines**

### **Standard Package Structure**
```
src/main/java/com/thefreelancer/microservices/{service}/
├── config/           # Security, Kafka, Database configs
├── controller/       # REST API endpoints
├── dto/             # Request/Response objects
├── event/           # Kafka event DTOs
├── exception/       # Custom exceptions & global handlers
├── listener/        # Kafka event listeners
├── model/           # JPA entities
├── repository/      # Data access layer
└── service/         # Business logic layer
```

### **API Design Standards**
- **RESTful** endpoints with proper HTTP methods
- **Consistent** response format with DTOs
- **Validation** using Bean Validation annotations
- **Error handling** with global exception handlers
- **Security** with JWT tokens (permit all during development)

### **Database Configuration**
- Each service has its **own PostgreSQL database**
- **Hibernate** auto-generates tables from entities
- **Connection pooling** and proper datasource configuration
- **Migration scripts** for production deployments

---

## 🎯 **Current Implementation Status**

### **Phase 1: Foundation** (In Progress)
- [x] Auth Service: User registration, basic CRUD ✅ **PARTIAL**
- [x] Gig Service: Event listener for profile creation ✅ **PARTIAL**  
- [x] Job Proposal Service: Database models and repositories ✅ **PARTIAL**
- [ ] JWT Authentication implementation
- [ ] Basic CRUD operations for all entities

### **Phase 2: Core Business Logic** (Planned)
- [ ] Job posting and proposal workflow
- [ ] Contract creation from accepted proposals
- [ ] Milestone-based payment system
- [ ] File upload for attachments and portfolio

### **Phase 3: Advanced Features** (Future)
- [ ] Search and filtering capabilities
- [ ] Real-time notifications
- [ ] Advanced security and rate limiting
- [ ] Analytics and reporting

---

## 🚀 **Development Workflow**

### **Starting the System**
1. **Start Infrastructure**: `docker-compose up -d` (Kafka, Zookeeper, PostgreSQL)
2. **Run Auth Service**: `cd auth-service && ./mvnw spring-boot:run`
3. **Run Gig Service**: `cd gig-service && ./mvnw spring-boot:run`
4. **Run Job Service**: `cd job-proposal-service && ./mvnw spring-boot:run`

### **Testing APIs**
```bash
# Register a user
curl -X POST http://localhost:8081/api/auth/register \
-H "Content-Type: application/json" \
-d '{"email":"test@example.com","password":"password123","name":"Test User"}'

# Check if profile was created (via Kafka event)
# Monitor logs in both auth-service and gig-service terminals
```

### **Database Access**
- **Auth DB**: `psql -h localhost -p 5433 -U admin -d auth_db`
- **Gig DB**: `psql -h localhost -p 5434 -U admin -d gig_db`  
- **Job DB**: `psql -h localhost -p 5435 -U admin -d job_proposal_db`

---

## 📝 **Implementation Checklist**

### **Before implementing any API:**
- [ ] Check `features.md` for the API specification
- [ ] Create/update the corresponding entity if needed
- [ ] Implement repository methods
- [ ] Create service layer with business logic
- [ ] Implement controller with proper validation
- [ ] Add error handling
- [ ] Test the endpoint manually
- [ ] **Update `features.md` as completed**

### **Code Quality Standards:**
- [ ] Use Lombok annotations for boilerplate code
- [ ] Add proper validation annotations (@NotNull, @Email, etc.)
- [ ] Include logging with SLF4J
- [ ] Handle exceptions gracefully
- [ ] Use DTOs for API requests/responses
- [ ] Follow Spring Boot best practices

---

## 🔧 **Troubleshooting**

### **Common Issues:**
1. **Database Connection**: Ensure PostgreSQL containers are running
2. **Kafka Connection**: Verify Kafka broker is accessible on localhost:9092
3. **Port Conflicts**: Check if services are running on correct ports
4. **Event Not Received**: Check Kafka logs and consumer group settings

### **Useful Commands:**
```bash
# Check running containers
docker ps

# View Kafka topics
docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --list

# Check database connections
docker logs auth-postgres-db
docker logs gig-postgres-db
```

---

## 📚 **Reference Links**

- **Features Tracking**: `features.md` 📊 **ALWAYS UPDATE THIS**
- **Database Schema**: See entity classes in each service
- **API Documentation**: Check controller classes for endpoint definitions
- **Event Definitions**: See event DTOs in each service

---

**Remember**: Always update `features.md` when completing any task! 🎯
