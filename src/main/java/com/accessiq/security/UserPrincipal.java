package com.accessiq.security;

import com.accessiq.model.Role;
import com.accessiq.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User principal implementation for Spring Security.
 */
public class UserPrincipal implements UserDetails {

    private final User user;

    /**
     * Constructs a new UserPrincipal.
     *
     * @param user the user entity
     */
    public UserPrincipal(final User user) {
        this.user = user;
    }

    /**
     * Gets the user ID.
     *
     * @return the user ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Gets the user email.
     *
     * @return the user email
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Gets the user entity.
     *
     * @return the user entity
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the authorities for the user.
     *
     * @return the collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final Set<Role> roles = user.getRoles();
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Gets the username (email).
     *
     * @return the username
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * Checks if the account is non-expired.
     *
     * @return true if non-expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Checks if the account is non-locked.
     *
     * @return true if non-locked
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Checks if the credentials are non-expired.
     *
     * @return true if non-expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Checks if the account is enabled.
     *
     * @return true if enabled
     */
    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}