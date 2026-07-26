package com.accessiq.dto;

import com.accessiq.model.RoleName;

import java.util.Set;

public class UserResponse {
    private Long id;
    private String email;
    private boolean enabled;
    private Set<RoleName> roles;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, boolean enabled, Set<RoleName> roles) {
        this.id = id;
        this.email = email;
        this.enabled = enabled;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<RoleName> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleName> roles) {
        this.roles = roles;
    }
}