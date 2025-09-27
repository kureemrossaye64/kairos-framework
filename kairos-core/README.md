# KAIROS - Core

The `kairos-core` module is the foundational layer of the Kairos Framework. It provides the essential building blocks, including common data models (entities), security configurations, and shared utilities that are used by nearly every other module in the system. Its primary purpose is to establish a stable and consistent base for the entire platform.

## Key Features

- **Data Persistence:** Establishes the core data persistence layer using **Spring Data JPA**. It defines the fundamental `BaseEntity` from which other data models in the framework inherit, ensuring consistency with ID generation and auditing fields.
- **Security Foundation:** Implements the application's security foundation using **Spring Security**. It includes support for stateless, token-based authentication using **JSON Web Tokens (JWT)**, which is essential for securing APIs in a distributed environment.
- **Shared Entities:** Contains the core business entities that are shared across different modules, such as `User` and `Role`. This centralization prevents code duplication and ensures a single source of truth for the main data models.
- **Repository Layer:** Defines the Spring Data JPA repositories for the core entities (e.g., `UserRepository`), providing a clean and standardized way to interact with the database.
- **Web and Configuration:** Includes `spring-boot-starter-web` to provide foundational web capabilities and serves as a central point for cross-cutting concerns and configurations.

## Core Components

- **Entities (`/entity`):** Contains the core JPA entities like `User`, `Role`, and `BaseEntity`. These entities define the primary database schema for the application.
- **Repositories (`/repository`):** Contains the Spring Data JPA repository interfaces for accessing the core entities (e.g., `UserRepository`).
- **Security (`/security`):** This package houses the security configuration, including the `JwtTokenProvider` for creating and validating JWTs, and the main `SecurityConfig` class that sets up firewall rules, authentication providers, and session management policies.
- **Configuration (`/config`):** Provides central configuration beans and settings that are applicable across the entire application.

## Dependencies

This module serves as a dependency for almost all other `kairos-*` modules, providing them with the necessary security, data, and utility classes. It integrates with several key external libraries:

- **Spring Boot:** For Data JPA, Security, and Web functionalities.
- **Hibernate:** As the underlying JPA provider.
- **JJWT (Java JWT):** For implementing JWT-based authentication.
- **Lombok:** To reduce boilerplate code in entities and other data objects.

By providing these fundamental services, the `kairos-core` module ensures that the entire Kairos Framework is built on a robust, secure, and maintainable foundation.