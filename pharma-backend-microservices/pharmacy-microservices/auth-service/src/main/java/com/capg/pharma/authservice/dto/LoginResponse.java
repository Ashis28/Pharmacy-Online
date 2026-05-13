package com.capg.pharma.authservice.dto;

import java.util.Set;

/**
 * Response DTO returned after a successful login.
 */
public class LoginResponse {

    private String token;
    private String name;
    private Long id;
    private Set<String> roles;

    public LoginResponse(String token, String name, Long id, Set<String> roles) {
        this.token = token;
        this.name = name;
        this.id = id;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
