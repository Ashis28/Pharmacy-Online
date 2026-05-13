package com.capg.pharma.authservice.model;

import jakarta.persistence.*;
import java.util.Set;

/**
 * JPA entity representing a registered user in the pharmacy platform.
 *
 * <p>Stored in the {@code users} table in {@code auth_db}. Roles are stored
 * in a separate {@code user_roles} collection table to support multiple roles
 * per user.</p>
 *
 * <p>Passwords are stored as BCrypt hashes — never in plain text.</p>
 */
@Entity
@Table(name = "users")
public class User {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Full display name of the user. */
    @Column(nullable = false)
    private String name;

    /** Unique email address used for login and JWT subject. */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. Never stored or returned in plain text. */
    @Column(nullable = false)
    private String password;

    /**
     * Set of roles assigned to this user.
     * Loaded eagerly so roles are available immediately after authentication.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles;

    /** @return the user's primary key */
    public Long getId() { return id; }
    /** @param id the primary key to set */
    public void setId(Long id) { this.id = id; }
    /** @return the user's display name */
    public String getName() { return name; }
    /** @param name the display name to set */
    public void setName(String name) { this.name = name; }
    /** @return the user's email address */
    public String getEmail() { return email; }
    /** @param email the email address to set */
    public void setEmail(String email) { this.email = email; }
    /** @return the BCrypt-hashed password */
    public String getPassword() { return password; }
    /** @param password the hashed password to set */
    public void setPassword(String password) { this.password = password; }
    /** @return the set of roles assigned to this user */
    public Set<Role> getRoles() { return roles; }
    /** @param roles the roles to assign */
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
