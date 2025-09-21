# The Freelancer Platform

A comprehensive microservices-based freelancing platform with AI-powered features for job matching, proposal enhancement, intelligent recommendations and marketplace analysis.

## 📋 Platform Overview

The Freelancer Platform is built using a microservices architecture consisting of 8 core services, each handling specific domain responsibilities. The platform facilitates secure job posting, proposal management, escrow-based payments, and collaborative workspaces.

### Key Features

- **AI-Enhanced Job Matching**: Semantic search with vector embeddings for optimal freelancer-job matching.
- **Escrow Payment System**: Secure Stripe-based payment processing with platform fee management.
- **Real-time Communication**: WebSocket-based messaging and notifications.
- **Collaborative Workspace**: Task management, file sharing, and calendar integration.
- **AI-Powered Content Enhancement**: Job descriptions, proposals, and profile optimization.

## 🏗️ Architecture

### Microservices Communication Diagram

```
                                    ┌─────────────────────┐
                                    │    Frontend App     │
                                    │    (Next.js)        │
                                    │     Port: 3000      │
                                    └──────────┬──────────┘
                                               │ HTTP/REST
                                               ▼
                    ┌─────────────────────────────────────────────────────────────────────┐
                    │                  API Gateway                                        │
                    │                  Port: 8080                                         │
                    │           JWT Validation & Routing                                  │
                    └┬──────────┬─────────┬───────────┬─────────────┬────────┬──────────┬─┘
                     │          │         │           │             │        │          │
                     ▼          ▼         ▼           ▼             ▼        ▼          ▼
            ┌─────────────┐ ┌──────┐ ┌──────┐     ┌─────────┐     ┌──────┐ ┌───────┐ ┌──────┐
            │Auth Service │ │ Gig  │ │ Job- │     │Workspace│     │Notify│ │Payment│ │ AI   │
            │Port: 8081   │ │8082  │ │Propol│     │Service  │     │8085  │ │8087   │ │8086  │
            │             │ │      │ │8083  │     │8084     │     │      │ │       │ │      │
            └──────┬──────┘ └───┬──┘ └───┬──┘     └───┬─────┘     └───┬──┘ └───┬───┘ └──┬───┘
                   │            │        │            │               │        │        │
                   ▼            ▼        ▼            ▼               ▼        ▼        ▼
         ┌─────────────────┐ ┌────────┐ ┌──────────┐┌──────────┐ ┌──────┐ ┌───────┐  ┌──────┐
         │   Auth DB       │ │Gig DB. │ │Job DB    ││Workspace │ │Notify│ │Payment│  │Redis │
         │  PostgreSQL     │ │pgvector│ │PostgreSQL││   DB     │ │  DB  │ │  DB   │  │Memory│
         │   Port: 5433    │ │5434    │ │5435      ││PostgreSQL│ │5438  │ │5437   │  │6379  │
         └─────────────────┘ └────────┘ └──────────┘│Port: 5436│ └──────┘ └───────┘  └──────┘
                                                    └──────────┘

                           ┌─────────────────────────────────────────┐
                           │            Message Broker               │
                           │         Apache Kafka + Zookeeper        │
                           │      Ports: 9092, 29092, 2181           │
                           │                                         │
                           │  Topics: User Events, Notifications,    │
                           │  Job Events, Payment Events             │
                           └─────────────────────────────────────────┘
                                               ▲
                                               │ Async Events
                            ┌──────────────────┼──────────────────┐
                            │                  │                  │
                            ▼                  ▼                  ▼
                    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
                    │ Auth Service │ │Notification  │ │Other Services│
                    │(User Created)│ │  Service     │ │ (Events)     │
                    └──────────────┘ └──────────────┘ └──────────────┘

                           ┌─────────────────────────────────────────┐
                           │           External Services             │
                           │                                         │
                           │  ┌─────────┐ ┌──────────┐ ┌──────────┐  │
                           │  │OpenAI   │ │ Stripe   │ │Cloudinary│  │
                           │  │API      │ │ Connect  │ │File      │  │
                           │  │(AI/ML)  │ │(Payments)│ │Storage   │  │
                           │  └─────────┘ └──────────┘ └──────────┘  │
                           └─────────────────────────────────────────┘
```

### Service Communication Matrix

| Service              | Communicates With           | Communication Type | Purpose                    |
|---------------------|----------------------------|-------------------|----------------------------|
| API Gateway         | All Services               | HTTP/REST         | Route & JWT validation     |
| Auth Service        | Kafka, Gig Service         | Kafka Events      | User profile creation      |
| Gig Service         | Auth, AI Service           | HTTP + Kafka      | Profile management         |
| Job-Proposal Service| Auth, Gig, Workspace, AI   | HTTP              | Job & proposal workflow    |
| Workspace Service   | Auth Service               | HTTP              | User authentication        |
| Notification Service| Auth, Job-Proposal, Gig, Workspace | Kafka + HTTP | Event notifications   |
| Payment Service     | Auth, Stripe API           | HTTP + Webhooks   | Payment processing         |
| AI Service          | Gig DB, OpenAI, Redis      | HTTP + DB         | AI features & chatbot      |

### Database Architecture

| Service                | Database          | Port | Special Features           |
|------------------------|-------------------|------|----------------------------|
| Auth Service           | auth_db           | 5433 | User authentication data   |
| Gig Service            | gig_db            | 5434 | **pgvector extension**     |
| Job-Proposal Service   | job_proposal_db   | 5435 | Job and proposal data      |
| Workspace Service      | workspace_db      | 5436 | Tasks, files, messaging    |
| Notification Service   | notification_db   | 5438 | Notification history       |
| Payment Service        | payment_db        | 5437 | Escrow and transactions    |
| AI Service             | Shared gig_db + Redis | 5434, 6379 | Vector ops + memory |

### Kafka Event Flow

```
User Registration → Auth Service → Kafka → Gig Service (Create Profile)
Job Posted → Job Service → Kafka → Notification Service → Email/WebSocket
Proposal Accepted → Job Service → Kafka → Notification Service + Workspace Service
Payment Completed → Payment Service → Kafka → Notification Service
```

### Service Overview

| Service                | Port/Role                        | Technology Stack                | Database     |
|------------------------|----------------------------------|---------------------------------|--------------|
| API Gateway            | Request routing, JWT validation  | Spring Boot, Maven              | N/A          |
| Auth Service           | Authentication & Authorization   | Spring Boot, JWT, Kafka         | PostgreSQL   |
| AI Service             | Chatbot, Content Enhancement     | OpenAI GPT-4o-mini, Redis       | Redis        |
| Notification Service   | Email & Real-time Notifications  | Spring Mail, Kafka, WebSocket   | PostgreSQL   |
| Workspace Service      | Task Management, File Upload, Messaging | Spring Boot, Cloudinary, WebSocket | PostgreSQL   |
| Payment Service        | Escrow System, Stripe Integration| Stripe API, Spring Boot         | PostgreSQL   |
| Job-Proposal Service   | Job Management, Proposal Processing | Spring Boot, AI Integration    | PostgreSQL   |
| Gig-Profile Service    | Profile & Gig Management, Semantic Search | pgvector, OpenAI Embeddings | PostgreSQL   |

### 1. API Gateway

Used to route requests to other services.

- The API gateway checks JWT tokens and extracts user information, which it sends to other services to check access.
- Some APIs are public and some APIs are role-specific.

### 2. Auth Service

Generates JWT access tokens and refresh tokens, which are then used by the API gateway to check authentication and authorization.

- Stores user basic information for login and Stripe.
- Allows access to user information using public APIs.
- When a user is registered, Kafka is used to create a profile in the gig-profile-service.

### 3. AI Service
Rag based chatbot with memory and basic AI prompting (rest AI is integrated in gig-services)

-Chatbot for new users that uses Redis to store memory for anonymous users.
- Some common questions are set as the knowledge base of the chatbot, and it can provide answers for them.
- A vector database is used to implement RAG with memory and rate limiting.
- Basic prompt engineering AI usages are implemented in this service and are used in the job and proposal sections( usage in detail in specific section).

**Technologies used:**

- Redis database
- OpenAI API (GPT-4o-mini)

### 4. Notification Service

Sends notifications and Emails for:

| Event                                      | Recipient    | Delivery Method         |
|---------------------------------------------|--------------|------------------------|
| Job accepted and escrow created             | Client       | Notification, Email     |
| Proposal accepted                          | Freelancer   | Notification            |
| Proposal submitted for a job                | Client       | Notification            |
| Invite sent for a job                       | Freelancer   | Notification            |
| Invite accepted, now complete payment       | Client       | Notification            |
| Job submitted                              | Client       | Notification, Email     |
| Job accepted and payment completed          | Freelancer   | Notification, Email     |
| Job completed, funds transferred, review    | Client       | Notification, Email     |

**Technologies used:**

- Spring Mail
- Kafka
- WebSocket

### 5. Workspace Service

Users can perform CRUD operations on tasks (sample Kanban board).

- Marks events for using the calendar for meetings or submissions.
- Handles payment and task submission, refunds to clients if needed, and breaks escrow.
- Submit necessary files.
- Workspace messaging and direct messaging (using WebSocket - not implemented in frontend).
- Download contract information and see contract details for that job.

**Technologies:**

- Cloudinary
- Websocket

### 6. Payment Service

Escrow-based payment service using Stripe for freelance marketplace.

- The client pays for the job as an escrow, and the freelancer receives the money when the job is done.
- The platform keeps 5% of the money.
- Creates the Stripe account and verifies the account on Stripe for a freelancer; otherwise, a freelancer can't apply for jobs.

**Technologies:**

- Stripe

### 7. Job-Proposal Service (Job Management Service)

Clients submit jobs with necessary attachments (files stored in Cloudinary). The jobs will then appear on freelancers' feeds using AI vector embedding matching with profiles and gigs.

**AI usage:**

- Job title enhancement using the job description.
- Job description enhancement.
- Skill extraction from the job description in case the client doesn't know which skills to add; these also help in finding better freelancers.
- **Technique:** Prompt templating OpenAI (GPT-4o-mini) from AI service.

Freelancers will apply proposals for jobs.

**AI usage:**

- The CV is enhanced/written based on the freelancer's profile information.
- Also analyzes the tone and the quality of the current proposal being submitted to help the freelancer.
- Uses AI service.
- **Technique:** Prompt engineering.

When the client picks a freelancer, a contract is created with the necessary information and a room is created in the workspace service.

After a job is completed, a notification is sent to the client to review the freelancer, and they can review the freelancer, which can be further used to enhance searching.

### 8. Gigs and Profile Service

Manages gigs and profiles, AI recomendation and marketplace analysis.

- A freelancer can create gigs and three tiers of gig packages under one gig.
- Clients can use semantic search for gigs and send a direct message to the freelancer and offer them a job.

**AI usage:**

- Good gigs with specific keywords help the freelancer's gigs pop up in the clients' searches, as semantic search with query enhancement is being used. First semantic search then filtering based on category.
- Updating profiles: a better profile with enriched information is good for the feed of the freelancer to find good jobs.
- AI is used to help clients find the best freelancers for their jobs using **80% semantic search and 20% business stats** (delivery rate, availability, total reviews × review score). This is a premium option that lets clients find the best freelancers fast.

**Future implementations that were incomplete:**

- Providing profile badges based on skills with the freelancer's completed jobs.
- Sentiment analysis of the latest reviews using AI for a specific freelancer.

**Marketplace analysis:** For a specific job and for a given sample size and depth, we analyze the demand for that specific job (not implemented in frontend).

**Technology:**

We are using a vector table (pgvector with OpenAI embedding) to store the gigs, profile, and jobs embeddings, and for any changes, updates, or deletes to these, the vector database is also updated. This vector database is used to find the matchings.

## 🛠️ General Technologies Used

- **Web client** for inter-service communication
- **Kafka, zookeeper, kafka UI** for notifications and some asynchronous communication between services
- **Separate PostgreSQL database** for all the services except AI service
- **Redis**
- **docker**
- Maven, JWT, Spring JPA, Spring Dev Tools
- **Swagger UI**
- To check APIs for a service go to: `http://localhost:<port>/swagger-ui.html`
- **Postman**

## 🚀 Deployment

**Current Status:** Docker-compose file to deploy in Azure VM, docker images uploaded at khalidtuhin3 dockerhub, but we were unable to deploy due to credit card issues and student account not providing a public IP.

## 💻 Frontend Technologies Used

- **Next.js**
- **TypeScript**
- **Axios**
- **React Hook Form**

## 📁 Project Structure

```text

the-freelancer/
├── api-gateway/              # API Gateway service
├── auth-service/             # Authentication & Authorization
├── ai-service/               # AI chatbot & content enhancement
├── notification-service/     # Email & real-time notifications
├── workspace-service/        # Task management & file sharing
├── payment-service/          # Stripe escrow system
├── job-proposal-service/     # Job & proposal management
├── gig-service/              # Gig & profile management
├── the-freelancer-frontend/  # Next.js frontend application
├── apis/                     # API documentation & testing
├── docker-compose.yml        # Multi-service deployment
├── features.md               # Feature implementation tracking
└── README.md                 # This file
```

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL
- Redis
- Stripe Account (for payments)
- OpenAI API Key (for AI features)
- Cloudinary Account (for file storage)

### Environment Setup

#### 1. Clone the repository

```bash

git clone <repository-url>
cd the-freelancer
```

#### 2. Set up environment variables

Each service requires specific environment variables for database connections, API keys, and service configurations.

#### 3. Run with Docker Compose

```bash

docker-compose up -d
```

#### 4. Individual Service Development

```bash
# Navigate to any service directory
cd auth-service
./mvnw spring-boot:run
```

### API Documentation

Access Swagger UI for each service:

- Service APIs available at: `http://localhost:<port>/swagger-ui.html`


### Note: This was added in Sep 21 ,no code changes were made
- Some apis are not available in the frontend as they weren't applied for time shortage, we request you to check the swagger ui or postman provided
