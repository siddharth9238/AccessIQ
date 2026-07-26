package com.accessiq.service;

import com.accessiq.exception.BadRequestException;
import com.accessiq.exception.ResourceNotFoundException;
import com.accessiq.model.Role;
import com.accessiq.model.RoleName;
import com.accessiq.model.User;
import com.accessiq.repository.RoleRepository;
import com.accessiq.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role employeeRole;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        employeeRole = new Role(RoleName.EMPLOYEE);
        managerRole = new Role(RoleName.MANAGER);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(employeeRole));
    }

    @Test
    void testCreateUserSuccess() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleName.EMPLOYEE))
            .thenReturn(Optional.of(employeeRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User user = userService.createUser(
            "test@example.com", "password", Set.of(RoleName.EMPLOYEE));

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUserEmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () ->
            userService.createUser(
                "test@example.com", "password", Set.of(RoleName.EMPLOYEE))
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUserWithNoRoles() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(BadRequestException.class, () ->
            userService.createUser("test@example.com", "password", null)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetByEmailSuccess() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        User user = userService.getByEmail("test@example.com");

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testGetByEmailNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            userService.getByEmail("nonexistent@example.com")
        );
    }

    @Test
    void testGetByIdSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User user = userService.getById(1L);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void testGetByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            userService.getById(1L)
        );
    }

    @Test
    void testHasRole() {
        testUser.setRoles(Set.of(employeeRole, managerRole));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        boolean result = userService.hasRole(1L, RoleName.EMPLOYEE);

        assertThat(result).isTrue();
    }

    @Test
    void testHasAnyRole() {
        testUser.setRoles(Set.of(managerRole));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        boolean result = userService.hasAnyRole(
            1L, RoleName.EMPLOYEE, RoleName.MANAGER);

        assertThat(result).isTrue();
    }
}