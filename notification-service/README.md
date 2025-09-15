# Notification Service

The Notification Service is a microservice that handles all notification functionalities for the FreelancerHub platform. It provides real-time notifications, email notifications, and manages notification delivery across the system.

## Features

### Core Functionality
- **Real-time Notifications**: WebSocket-based instant notifications
- **Email Notifications**: Automated email delivery for important events
- **Notification Management**: CRUD operations for notifications
- **Status Tracking**: Track notification delivery status and retry failed notifications
- **Multi-channel Delivery**: Support for WebSocket, Email, and future push notifications

### Notification Types
- `PROPOSAL_SUBMITTED`: When a freelancer submits a proposal
- `PROPOSAL_ACCEPTED`: When a client accepts a proposal
- `PROPOSAL_REJECTED`: When a client rejects a proposal
- `JOB_POSTED`: When a new job is posted
- `CONTRACT_CREATED`: When a contract is created
- `MILESTONE_COMPLETED`: When a milestone is completed
- `PAYMENT_RELEASED`: When payment is released
- `SYSTEM_ANNOUNCEMENT`: System-wide announcements

### Event-Driven Architecture
- **Kafka Integration**: Listens to events from other microservices
- **Asynchronous Processing**: Non-blocking notification delivery
- **Retry Mechanism**: Automatic retry for failed notifications
- **Dead Letter Queue**: Handle permanently failed notifications

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Other Services │───▶│  Kafka Topics   │───▶│ Notification    │
│  (Events)       │    │                 │    │ Service         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                              ┌─────────────────────────┼─────────────────────────┐
                              │                         │                         │
                              ▼                         ▼                         ▼
                    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
                    │   WebSocket     │    │   Email         │    │   Push          │
                    │   (Real-time)   │    │   Service       │    │   Notifications │
                    └─────────────────┘    └─────────────────┘    └─────────────────┘
```

## API Endpoints

### User Notifications
```http
GET /api/notifications/user/{userId}              # Get user notifications (paginated)
GET /api/notifications/user/{userId}/unread       # Get unread notifications
GET /api/notifications/user/{userId}/unread/count # Get unread count
PUT /api/notifications/{notificationId}/read      # Mark notification as read
PUT /api/notifications/user/{userId}/read-all     # Mark all notifications as read
```

### Internal Service APIs
```http
POST /api/notifications/internal/proposal-submitted   # Create proposal submitted notification
POST /api/notifications/internal/proposal-accepted    # Create proposal accepted notification
POST /api/notifications/internal/proposal-rejected    # Create proposal rejected notification
POST /api/notifications/internal/contract-created     # Create contract created notification
POST /api/notifications/internal/milestone-completed  # Create milestone completed notification
POST /api/notifications/internal/payment-released     # Create payment released notification
```

### WebSocket Endpoints
```
/ws/notifications                    # WebSocket connection endpoint
/topic/notifications/{userId}        # Subscribe to user-specific notifications
```

## Configuration

### Environment Variables
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5436
DB_NAME=notification_db
DB_USERNAME=notification_user
DB_PASSWORD=notification_password

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Email Configuration
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

# Service URLs
AUTH_SERVICE_URL=http://localhost:8081
JOB_PROPOSAL_SERVICE_URL=http://localhost:8083
GIG_SERVICE_URL=http://localhost:8082
```

### application.properties
Key configurations are externalized and can be overridden via environment variables.

## Database Schema

### Notifications Table
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    sender_id BIGINT,
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    job_id BIGINT,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    status notification_status DEFAULT 'PENDING',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    delivered_at TIMESTAMP,
    retry_count INTEGER DEFAULT 0
);
```

## Kafka Topics

### Consumed Topics
- `proposal-submitted`: When freelancer submits proposal
- `proposal-accepted`: When client accepts proposal  
- `proposal-rejected`: When client rejects proposal
- `job-posted`: When new job is posted
- `contract-created`: When contract is created
- `milestone-completed`: When milestone is completed
- `payment-released`: When payment is released

### Event Formats

#### ProposalSubmittedEvent
```json
{
  "proposalId": 123,
  "jobId": 456,
  "freelancerId": 789,
  "clientId": 101,
  "jobTitle": "Website Development",
  "freelancerName": "John Doe",
  "freelancerHandle": "@johndoe",
  "submittedAt": "2024-01-15T10:30:00Z",
  "proposalCoverLetter": "I am interested in this project...",
  "totalBudget": 5000,
  "currency": "USD",
  "deliveryDays": 30
}
```

## Running the Service

### Using Docker Compose
```bash
# Start all services including dependencies
docker-compose up -d

# View logs
docker-compose logs -f notification-service

# Stop services
docker-compose down
```

### Local Development
```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Start the service
./mvnw spring-boot:run
```

### Prerequisites
- Java 21+
- Maven 3.6+
- PostgreSQL 15+ (or Docker)
- Kafka (or Docker)

## WebSocket Client Example

### JavaScript Client
```javascript
// Connect to WebSocket
const socket = new SockJS('/ws/notifications');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to user notifications
    stompClient.subscribe('/topic/notifications/' + userId, function (notification) {
        const notificationData = JSON.parse(notification.body);
        displayNotification(notificationData);
    });
});

function displayNotification(notification) {
    // Display notification in UI
    console.log('New notification:', notification);
}
```

## Email Templates

Email notifications use simple text templates that can be enhanced with HTML templates in the future.

### Basic Email Structure
```
Subject: {notification.title}

Hello,

{notification.message}

Job ID: {notification.jobId} (if applicable)

You can view more details by logging into your FreelancerHub account.

Best regards,
FreelancerHub Team
```

## Monitoring and Health Checks

### Health Check Endpoint
```http
GET /actuator/health
```

### Metrics
- Notification creation rate
- Delivery success rate
- Failed notification count
- WebSocket connection count
- Email delivery metrics

## Inter-Service Communication

### Calling Other Services
The notification service enriches notifications with data from other services:
- **Auth Service**: User details (name, email, handle)
- **Job Service**: Job details (title, description)
- **Proposal Service**: Proposal details

### Example Service Calls
```java
// Get user details from auth service
WebClient.create(authServiceUrl)
    .get()
    .uri("/api/users/{userId}", userId)
    .retrieve()
    .bodyToMono(UserDto.class);
```

## Future Enhancements

### Planned Features
1. **Push Notifications**: Mobile push notifications via Firebase
2. **SMS Notifications**: Text message notifications for critical events
3. **Notification Preferences**: User-configurable notification settings
4. **Rich Email Templates**: HTML email templates with branding
5. **Notification Analytics**: Delivery metrics and user engagement tracking
6. **Batch Processing**: Bulk notification processing for system announcements

### Performance Optimizations
1. **Notification Batching**: Group notifications for better performance
2. **Caching**: Cache user preferences and frequently accessed data
3. **Database Partitioning**: Partition notifications by date for better query performance
4. **Message Queuing**: Enhanced queue management for high-volume scenarios

## Contributing

1. Follow the existing code style and patterns
2. Add unit tests for new functionality
3. Update documentation for API changes
4. Test WebSocket and email functionality thoroughly
5. Consider performance impact of notification volume

## Troubleshooting

### Common Issues

1. **WebSocket Connection Failed**
   - Check if port 8084 is accessible
   - Verify WebSocket configuration
   - Check browser CORS settings

2. **Email Not Sending**
   - Verify SMTP configuration
   - Check email credentials
   - Review firewall settings

3. **Kafka Connection Issues**
   - Ensure Kafka is running on port 9092
   - Check topic creation
   - Verify consumer group configuration

4. **Database Connection Failed**
   - Verify PostgreSQL is running on port 5436
   - Check database credentials
   - Ensure database exists

### Logs Location
```bash
# Docker logs
docker-compose logs notification-service

# Application logs
tail -f logs/notification-service.log
```
