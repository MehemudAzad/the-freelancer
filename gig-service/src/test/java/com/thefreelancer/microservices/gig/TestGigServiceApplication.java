package com.thefreelancer.microservices.gig;

import org.springframework.boot.SpringApplication;

public class TestGigServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(GigServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
