package com.accessiq.repository;

import com.accessiq.model.RoleName;
import com.accessiq.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        roleRepository.save(new com.accessiq.model.Role(RoleName.EMPLOYEE));
        roleRepository.save(new com.accessiq.model.Role(RoleName.MANAGER));
        
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRoles(Set.of(roleRepository.findByName(RoleName.EMPLOYEE).orElseThrow()));
        testUser = userRepository.save(testUser);
    }

    @Test
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail() {
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testSaveUser() {
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("password");
        newUser.setRoles(Set.of(roleRepository.findByName(RoleName.MANAGER).orElseThrow()));

        User saved = userRepository.save(newUser);

        assertNotNull(saved.getId());
        assertEquals("newuser@example.com", saved.getEmail());
    }
}