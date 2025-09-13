package com.thefreelancer.microservices.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thefreelancer.microservices.auth.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByHandle(String handle);
    Optional<User> findTopByHandleIgnoreCaseStartingWith(String handlePrefix);
}
