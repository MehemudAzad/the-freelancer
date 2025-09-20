package com.thefreelancer.microservices.gateway.routes;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class Routes {

	private static final String AUTH_SERVICE_URL = "http://localhost:8081";
	private static final String GIG_SERVICE_URL = "http://localhost:8082";
	private static final String JOB_PROPOSAL_SERVICE_URL = "http://localhost:8083";
	private static final String WORKSPACE_SERVICE_URL = "http://localhost:8084";
	private static final String NOTIFICATION_SERVICE_URL = "http://localhost:8085";
	private static final String AI_SERVICE_URL = "http://localhost:8086";
	private static final String PAYMENT_SERVICE_URL = "http://localhost:8087";

	@Bean
	public RouterFunction<ServerResponse> authServiceRoute() {
		return GatewayRouterFunctions.route("auth-service")
				.route(RequestPredicates.path("/api/auth/**"), this::forwardToAuthService)
				.build();
	}

	// CRITICAL: This bean name starts with 'a' to ensure it runs FIRST alphabetically
	@Bean
	public RouterFunction<ServerResponse> aGigServiceSpecificRoutes() {
		return GatewayRouterFunctions.route("gig-service-specific")
				// Marketplace Analysis APIs - HIGH PRIORITY
				.route(RequestPredicates.path("/api/marketplace/**"), this::forwardToGigService)
				
				// Job Matching APIs - must come BEFORE broad /api/jobs/** pattern
				.route(RequestPredicates.path("/api/jobs/match-freelancers"), this::forwardToGigService)
				.route(RequestPredicates.path("/api/jobs/*/match-freelancers"), this::forwardToGigService)
				.route(RequestPredicates.path("/api/jobs/*/match-freelancers/smart"), this::forwardToGigService)
				.route(RequestPredicates.path("/api/jobs/*/bulk-match"), this::forwardToGigService)
				
				// Freelancer APIs
				.route(RequestPredicates.path("/api/freelancers/*/job-feed"), this::forwardToGigService)
				.route(RequestPredicates.path("/api/freelancers/*/clear-cache"), this::forwardToGigService)
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> gigServiceRoute() {
		return GatewayRouterFunctions.route("gig-service")
				.route(RequestPredicates.path("/api/gigs/**"), this::forwardToGigService)
				.route(RequestPredicates.path("/api/reviews/**"), this::forwardToGigService)
				.route(RequestPredicates.path("/api/recommendations/**"), this::forwardToGigService)
				.route(RequestPredicates.path("/api/internal/embeddings/**"), this::forwardToGigService)
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> profileServiceRoute() {
		return GatewayRouterFunctions.route("profile-service")
				.route(RequestPredicates.path("/api/profiles/**"), this::forwardToGigService)
				.build();
	}

	// This bean name starts with 'z' to ensure it runs LAST alphabetically
	@Bean
	public RouterFunction<ServerResponse> zJobProposalServiceRoute() {
		return GatewayRouterFunctions.route("job-proposal-service")
				.route(RequestPredicates.path("/api/jobs/**"), this::forwardToJobProposalService)
				.route(RequestPredicates.path("/api/proposals/**"), this::forwardToJobProposalService)
				.route(RequestPredicates.path("/api/contracts/**"), this::forwardToJobProposalService)
				.route(RequestPredicates.path("/api/invites/**"), this::forwardToJobProposalService)
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> workspaceServiceRoute() {
		return GatewayRouterFunctions.route("workspace-service")
				.route(RequestPredicates.path("/api/workspaces/**"), this::forwardToWorkspaceService)
				.route(RequestPredicates.path("/api/direct-messages/**"), this::forwardToWorkspaceService)
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> notificationServiceRoute() {
		return GatewayRouterFunctions.route("notification-service")
				.route(RequestPredicates.path("/api/notifications/**"), this::forwardToNotificationService)
				.build();
	}
	

	@Bean
	public RouterFunction<ServerResponse> paymentServiceRoute() {
		return GatewayRouterFunctions.route("payment-service")
				.route(RequestPredicates.path("/api/payments/**"), this::forwardToPaymentService)
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> aiServiceRoute() {
		return GatewayRouterFunctions.route("ai-service")
				.route(RequestPredicates.path("/api/ai/**"), this::forwardToAiService)
				.build();
	}

	
	private ServerResponse forwardToAuthService(ServerRequest request) {
		return forwardWithUserContext(request, AUTH_SERVICE_URL);
	}

	private ServerResponse forwardToGigService(ServerRequest request) {
		return forwardWithUserContext(request, GIG_SERVICE_URL);
	}

	private ServerResponse forwardToJobProposalService(ServerRequest request) {
		return forwardWithUserContext(request, JOB_PROPOSAL_SERVICE_URL);
	}

	private ServerResponse forwardToWorkspaceService(ServerRequest request) {
		return forwardWithUserContext(request, WORKSPACE_SERVICE_URL);
	}

	private ServerResponse forwardToNotificationService(ServerRequest request) {
		return forwardWithUserContext(request, NOTIFICATION_SERVICE_URL);
	}

	private ServerResponse forwardToPaymentService(ServerRequest request) {
		return forwardWithUserContext(request, PAYMENT_SERVICE_URL);
	}

	private ServerResponse forwardToAiService(ServerRequest request) {
		return forwardWithUserContext(request, AI_SERVICE_URL);
	}

	private ServerResponse forwardWithUserContext(ServerRequest request, String targetUrl) {
		ServerRequest.Builder builder = ServerRequest.from(request);

		request.attribute("X-User-Id").ifPresent(userId -> builder.header("X-User-Id", userId.toString()));
		request.attribute("X-User-Email").ifPresent(userEmail -> builder.header("X-User-Email", userEmail.toString()));
		request.attribute("X-User-Role").ifPresent(userRole -> builder.header("X-User-Role", userRole.toString()));

		try {
			return HandlerFunctions.http(targetUrl).handle(builder.build());
		} catch (Exception e) {
			// This is a simplified error handling. In a real app, you'd want more robust logging.
			return ServerResponse.status(500).body("Error forwarding request: " + e.getMessage());
		}
	}
}
