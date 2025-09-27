package com.kairos.core.repository;

import com.kairos.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the User entity.
 * This interface provides full CRUD functionality out of the box.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Finds a user by their unique username.
     * @param username The username to search for.
     * @return an Optional containing the user if found, or an empty Optional otherwise.
     */
    Optional<User> findByUsername(String username);
}