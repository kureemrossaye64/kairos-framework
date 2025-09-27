package com.kairos.core.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a user of the application.
 * Stored in the "app_user" table as "user" is often a reserved SQL keyword.
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
public class User extends BaseEntity implements UserDetails {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(nullable = false)
    private String role; // Using simple String-based roles, e.g., "ROLE_USER", "ROLE_ADMIN"

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(new SimpleGrantedAuthority("ADMIN"));
	}

}