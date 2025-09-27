package com.kairos.core.repository;

import com.kairos.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link User} entity.
 * This interface provides full CRUD (Create, Read, Update, Delete) functionality
 * for User objects, managed by Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Finds a user by their unique username.
     * This is a custom query method automatically implemented by Spring Data JPA
     * based on the method name.
     *
     * @param username The username to search for.
     * @return an {@link Optional} containing the user if found, or an empty {@link Optional} otherwise.
     */
    Optional<User> findByUsername(String username);
}