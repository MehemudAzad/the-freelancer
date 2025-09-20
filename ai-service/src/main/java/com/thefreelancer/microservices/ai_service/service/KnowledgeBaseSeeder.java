package com.thefreelancer.microservices.ai_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds the knowledge base with initial content for the RAG chatbot
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseSeeder implements CommandLineRunner {
    
    private final KnowledgeBaseService knowledgeBaseService;
    private final JdbcTemplate jdbcTemplate;
    
    @Value("${app.knowledge-base.seed:true}")
    private boolean shouldSeed;
    
    @Override
    public void run(String... args) {
        if (!shouldSeed) {
            log.info("Knowledge base seeding is disabled");
            return;
        }
        
        // Add a small delay to ensure database is fully initialized
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Retry logic for database initialization
        int maxRetries = 5;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Attempting to seed knowledge base (attempt {}/{})", attempt, maxRetries);
                
                // Check if knowledge_base table exists first
                if (!checkTableExists()) {
                    log.warn("Knowledge base table does not exist on attempt {}. Waiting for schema initialization...", attempt);
                    if (attempt < maxRetries) {
                        Thread.sleep(3000); // Wait 3 seconds before retry
                        continue;
                    } else {
                        log.error("Knowledge base table still does not exist after {} attempts. Please check schema initialization.", maxRetries);
                        return;
                    }
                }
                
                // Check if knowledge base already has content
                Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM knowledge_base", Integer.class);
                if (count != null && count > 0) {
                    log.info("Knowledge base already contains {} entries, skipping seeding", count);
                    return;
                }
                
                log.info("Seeding knowledge base with initial content...");
                seedInitialKnowledge();
                log.info("Knowledge base seeding completed successfully");
                return; // Success, exit retry loop
                
            } catch (Exception e) {
                log.error("Failed to seed knowledge base on attempt {}: {}", attempt, e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(5000); // Wait 5 seconds before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    log.error("Failed to seed knowledge base after {} attempts", maxRetries, e);
                }
            }
        }
    }
    
    /**
     * Check if the knowledge_base table exists
     */
    private boolean checkTableExists() {
        try {
            String sql = "SELECT EXISTS (" +
                        "SELECT FROM information_schema.tables " +
                        "WHERE table_schema = 'public' " +
                        "AND table_name = 'knowledge_base'" +
                        ")";
            Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class);
            return exists != null && exists;
        } catch (Exception e) {
            log.warn("Could not check if knowledge_base table exists: {}", e.getMessage());
            return false;
        }
    }
    
    private void seedInitialKnowledge() {
        // Getting Started - Client
        knowledgeBaseService.addKnowledgeEntry(
            "How to get started as a client",
            "To get started as a client on The Freelancer platform: " +
            "1. Sign up with your email and verify your account " +
            "2. Complete your client profile with company information " +
            "3. Browse freelancers using filters or post a job " +
            "4. Use project templates (SaaS MVP, Mobile App, etc.) to define scope " +
            "5. Set up milestones with clear deliverables and deadlines " +
            "6. Fund milestones using secure escrow payments " +
            "7. Start collaborating with freelancers in the workspace",
            "guide",
            List.of("getting-started", "client", "signup", "onboarding")
        );
        
        // Getting Started - Freelancer
        knowledgeBaseService.addKnowledgeEntry(
            "How to get started as a freelancer",
            "To get started as a freelancer on The Freelancer platform: " +
            "1. Create your account and verify your email " +
            "2. Complete your profile with skills, experience, and portfolio " +
            "3. Add verified badges by taking skill tests " +
            "4. Create gigs showcasing your services and expertise " +
            "5. Browse available jobs and submit compelling proposals " +
            "6. Build your reputation through successful project completion " +
            "7. Use the workspace tools for seamless collaboration",
            "guide",
            List.of("getting-started", "freelancer", "signup", "profile", "gigs")
        );
        
        // Job Posting
        knowledgeBaseService.addKnowledgeEntry(
            "How to post a job",
            "To post a job on The Freelancer platform: " +
            "1. Click 'Post a Job' from your client dashboard " +
            "2. Select a project template (SaaS MVP, Shopify App, Next.js Site, etc.) " +
            "3. Define your project scope and requirements clearly " +
            "4. Break the project into logical milestones with specific deliverables " +
            "5. Set budget (fixed price or hourly) and timeline " +
            "6. Add technical requirements, design files, or specifications " +
            "7. Configure repo access rules if needed (read/write permissions) " +
            "8. Enable NDA and IP assignment if required " +
            "9. Publish your job and start receiving proposals",
            "guide",
            List.of("job-posting", "client", "templates", "milestones", "requirements")
        );
        
        // Payments and Escrow
        knowledgeBaseService.addKnowledgeEntry(
            "How payments work - Escrow system",
            "The Freelancer platform uses a secure escrow system for all payments: " +
            "For Fixed-Price Projects: Funds are deposited into escrow when milestones are funded. " +
            "Money is held securely until the client accepts the deliverable, then automatically released to the freelancer. " +
            "For Hourly Projects: Weekly timesheets are submitted and approved, with automatic billing and payments. " +
            "All payments are processed through Stripe Connect for maximum security. " +
            "Disputes can be raised if there are issues, with platform mediation available. " +
            "This system protects both clients and freelancers throughout the project lifecycle.",
            "feature",
            List.of("payments", "escrow", "security", "milestones", "stripe", "protection")
        );
        
        // Milestones
        knowledgeBaseService.addKnowledgeEntry(
            "What are milestones and how do they work",
            "Milestones are project checkpoints that break work into manageable, payable chunks. " +
            "Each milestone includes: A clear deliverable description, Definition of Done (DoD) checklist, " +
            "Payment amount and due date, Acceptance criteria for completion. " +
            "Milestone workflow: Client funds milestone → Freelancer works on deliverable → " +
            "Freelancer submits work → Client reviews against DoD → " +
            "Client accepts or requests revisions → Payment released automatically. " +
            "This system ensures clear expectations and secure payments for both parties.",
            "feature",
            List.of("milestones", "project-management", "deliverables", "payments", "workflow")
        );
        
        // Workspace and Collaboration
        knowledgeBaseService.addKnowledgeEntry(
            "Collaboration workspace features",
            "Each project has a dedicated workspace (Job Room) with comprehensive collaboration tools: " +
            "Real-time chat with threaded discussions and file uploads, " +
            "GitHub/GitLab integration showing PRs and CI status, " +
            "Task board with Kanban-style milestone tracking, " +
            "Shared calendar for deadlines and meeting scheduling, " +
            "File sharing and version control, " +
            "Video call integration for standups and reviews, " +
            "Preview links for deployed applications, " +
            "All communication and project assets organized in one place for easy management.",
            "feature",
            List.of("workspace", "collaboration", "communication", "github", "tasks", "files")
        );
        
        // Proposals and Matching
        knowledgeBaseService.addKnowledgeEntry(
            "How to write winning proposals",
            "To create compelling proposals that win projects: " +
            "1. Read the job description carefully and address specific requirements " +
            "2. Demonstrate understanding by asking clarifying questions " +
            "3. Break down your approach into clear milestones matching the client's needs " +
            "4. Provide realistic timelines and competitive pricing " +
            "5. Showcase relevant portfolio work and case studies " +
            "6. Highlight your verified skills and badges " +
            "7. Include mockups, prototypes, or detailed technical specifications " +
            "8. Maintain professional communication and responsiveness",
            "guide",
            List.of("proposals", "freelancer", "bidding", "portfolio", "pricing", "communication")
        );
        
        // Platform Features
        knowledgeBaseService.addKnowledgeEntry(
            "Key platform features and tools",
            "The Freelancer platform offers comprehensive features for successful project delivery: " +
            "Smart matching algorithm based on skills, experience, and project requirements, " +
            "Project templates for common development needs (SaaS, Mobile, Web, etc.), " +
            "Integrated development tools with GitHub/GitLab connectivity, " +
            "Automated testing and deployment previews, " +
            "Secure payment processing with escrow protection, " +
            "Dispute resolution system with expert mediation, " +
            "Performance tracking and reputation scoring, " +
            "24/7 customer support and platform assistance.",
            "feature",
            List.of("features", "platform", "tools", "matching", "integration", "support")
        );
        
        // Pricing and Fees
        knowledgeBaseService.addKnowledgeEntry(
            "Platform pricing and fees",
            "The Freelancer platform uses transparent, competitive pricing: " +
            "For Freelancers: Platform fee is deducted from earnings (competitive with industry standards), " +
            "For Clients: No additional fees beyond project costs, " +
            "Payment processing fees are minimal and clearly disclosed, " +
            "Premium features available for enhanced visibility and tools, " +
            "No hidden charges or surprise fees, " +
            "All fees are clearly shown before any transaction is completed.",
            "policy",
            List.of("pricing", "fees", "costs", "transparent", "freelancer", "client")
        );
        
        // Dispute Resolution
        knowledgeBaseService.addKnowledgeEntry(
            "How dispute resolution works",
            "Our fair dispute resolution process protects both parties: " +
            "Step 1: Direct negotiation between client and freelancer in the workspace, " +
            "Step 2: Platform mediation with support team review of all communications and deliverables, " +
            "Step 3: Expert technical review (optional paid service) for code quality assessment, " +
            "Step 4: Final platform decision based on evidence and milestone criteria, " +
            "All chat logs, files, commits, and milestone definitions are used as evidence. " +
            "The goal is always fair resolution that considers the work completed and original requirements.",
            "policy",
            List.of("disputes", "resolution", "mediation", "fairness", "evidence", "support")
        );
    }
}