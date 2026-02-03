package com.accessiq.service;

import com.accessiq.exception.BadRequestException;
import com.accessiq.exception.ResourceNotFoundException;
import com.accessiq.model.Role;
import com.accessiq.model.RoleName;
import com.accessiq.model.User;
import com.accessiq.repository.RoleRepository;
import com.accessiq.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String email, String rawPassword, Set<RoleName> roleNames) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists: " + email);
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        Set<Role> roles = roleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName)))
                .collect(Collectors.toSet());
        user.setRoles(roles);
        return userRepository.save(user);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
