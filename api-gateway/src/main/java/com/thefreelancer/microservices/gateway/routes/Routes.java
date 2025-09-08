package com.thefreelancer.microservices.gateway.routes;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class Routes {
	@Bean
	public RouterFunction<ServerResponse> authServiceRoute(){
		return GatewayRouterFunctions.route("auth-service")
			.route(RequestPredicates.path("/api/auth/**"), 
				request -> {
					// Forward to auth service without modification
					return HandlerFunctions.http("http://localhost:8081").handle(request);
				})
			.build();
	}

    @Bean
	public RouterFunction<ServerResponse> gigServiceRoute(){
		return GatewayRouterFunctions.route("gig-service")
			.route(RequestPredicates.path("/api/gigs/**"), 
				request -> {
					// Create handler function that forwards user context headers
					return HandlerFunctions.http("http://localhost:8082")
						.handle(request);
				})
			.build();
	}

    @Bean
	public RouterFunction<ServerResponse> jobProposalServiceRoute(){
		return GatewayRouterFunctions.route("job-proposal-service")
			.route(RequestPredicates.path("/api/jobs/**"), 
				request -> {
					// Create handler function that forwards user context headers
					return HandlerFunctions.http("http://localhost:8083")
						.handle(request);
				})
			.build();
	}
}
