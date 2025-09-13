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
	private static final String PAYMENT_SERVICE_URL = "http://localhost:8087";

	@Bean
	public RouterFunction<ServerResponse> authServiceRoute() {
		return GatewayRouterFunctions.route("auth-service")
				.route(RequestPredicates.path("/api/auth/**"), this::forwardToAuthService)
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> gigServiceRoute() {
		return GatewayRouterFunctions.route("gig-service")
				.route(RequestPredicates.path("/api/gigs/**"), this::forwardToGigService)
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> profileServiceRoute() {
		return GatewayRouterFunctions.route("profile-service")
				.route(RequestPredicates.path("/api/profiles/**"), this::forwardToGigService)
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> jobProposalServiceRoute() {
		return GatewayRouterFunctions.route("job-proposal-service")
				.route(RequestPredicates.path("/api/jobs/**"), this::forwardToJobProposalService)
				.route(RequestPredicates.path("/api/proposals/**"), this::forwardToJobProposalService)
				.route(RequestPredicates.path("/api/contracts/**"), this::forwardToJobProposalService)
				.build();
			}

	@Bean
	public RouterFunction<ServerResponse> workspaceServiceRoute() {
		return GatewayRouterFunctions.route("workspace-service")
				.route(RequestPredicates.path("/api/workspaces/**"), this::forwardToWorkspaceService)
				.build();
	}

	@Bean
	public RouterFunction<ServerResponse> paymentServiceRoute() {
		return GatewayRouterFunctions.route("payment-service")
				.route(RequestPredicates.path("/api/payments/**"), this::forwardToPaymentService)
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

	private ServerResponse forwardToPaymentService(ServerRequest request) {
		return forwardWithUserContext(request, PAYMENT_SERVICE_URL);
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
