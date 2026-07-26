package com.accessiq.service;

import com.accessiq.dto.UserResponse;
import com.accessiq.exception.BadRequestException;
import com.accessiq.exception.ResourceNotFoundException;
import com.accessiq.model.RoleName;
import com.accessiq.model.User;
import com.accessiq.repository.RoleRepository;
import com.accessiq.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for user management operations.
 */
@Service
public class UserService {

    /** Logger for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    /** The user repository. */
    private final UserRepository userRepository;

    /** The role repository. */
    private final RoleRepository roleRepository;

    /** The password encoder. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new UserService.
     *
     * @param userRepository the user repository
     * @param roleRepository the role repository
     * @param passwordEncoder the password encoder
     */
    public UserService(final UserRepository userRepository,
            final RoleRepository roleRepository,
            final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user.
     *
     * @param email the user email
     * @param rawPassword the raw password
     * @param roleNames the role names
     * @return the created user
     */
    @Transactional
    public User createUser(final String email, final String rawPassword,
            final Set<RoleName> roleNames) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists: " + email);
        }
        if (roleNames == null || roleNames.isEmpty()) {
            throw new BadRequestException("User must have at least one role");
        }
        final User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        final Set<RoleName> roles = roleNames;
        user.setRoles(roles.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Role not found: " + roleName)))
                .collect(Collectors.toSet()));
        return userRepository.save(user);
    }

    /**
     * Gets a user by email.
     *
     * @param email the user email
     * @return the user
     */
    @Transactional(readOnly = true)
    public User getByEmail(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }

    /**
     * Gets a user by ID.
     *
     * @param id the user ID
     * @return the user
     */
    @Transactional(readOnly = true)
    public User getById(final Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + id));
    }

    /**
     * Gets all users.
     *
     * @param pageable the pageable
     * @return the page of users
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(final Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Gets all user responses.
     *
     * @param pageable the pageable
     * @return the page of user responses
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUserResponses(final Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.isEnabled(),
                        user.getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toSet())
                ));
    }

    /**
     * Updates a user.
     *
     * @param id the user ID
     * @param email the new email
     * @param roleNames the new role names
     * @return the updated user
     */
    @Transactional
    public User updateUser(final Long id, final String email,
            final Set<RoleName> roleNames) {
        final User existingUser = getById(id);
        if (email != null && !email.equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new BadRequestException("Email already exists: " + email);
            }
            existingUser.setEmail(email);
        }
        if (roleNames != null && !roleNames.isEmpty()) {
            final Set<RoleName> roles = roleNames;
            existingUser.setRoles(roles.stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Role not found: " + roleName)))
                    .collect(Collectors.toSet()));
        }
        return userRepository.save(existingUser);
    }

    /**
     * Deletes a user.
     *
     * @param id the user ID
     */
    @Transactional
    public void deleteUser(final Long id) {
        final User user = getById(id);
        userRepository.delete(user);
    }

    /**
     * Checks if user has a role.
     *
     * @param userId the user ID
     * @param roleName the role name
     * @return true if user has the role
     */
    @Transactional(readOnly = true)
    public boolean hasRole(final Long userId, final RoleName roleName) {
        final User user = getById(userId);
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    /**
     * Checks if user has any of the roles.
     *
     * @param userId the user ID
     * @param roleNames the role names
     * @return true if user has any of the roles
     */
    @Transactional(readOnly = true)
    public boolean hasAnyRole(final Long userId, final RoleName... roleNames) {
        final User user = getById(userId);
        final Set<RoleName> requiredRoles = Set.of(roleNames);
        return user.getRoles().stream()
                .anyMatch(role -> requiredRoles.contains(role.getName()));
    }
}