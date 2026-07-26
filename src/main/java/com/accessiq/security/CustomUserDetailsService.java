package com.accessiq.security;

import com.accessiq.model.User;
import com.accessiq.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom user details service for loading users by email.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    /**
     * Constructs a new CustomUserDetailsService.
     *
     * @param userRepository the user repository
     */
    public CustomUserDetailsService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username (email).
     *
     * @param username the username (email)
     * @return the user details
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException {
        final User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new UserPrincipal(user);
    }
}