package com.accessiq.config;

import com.accessiq.model.Role;
import com.accessiq.model.RoleName;
import com.accessiq.model.User;
import com.accessiq.model.WorkflowDefinition;
import com.accessiq.model.WorkflowStepDefinition;
import com.accessiq.repository.RoleRepository;
import com.accessiq.repository.UserRepository;
import com.accessiq.repository.WorkflowDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Data initializer for bootstrap data.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final WorkflowDefinitionRepository workflowRepository;

    private final PasswordEncoder passwordEncoder;

    /** Admin email for bootstrap user. */
    @Value("${accessiq.bootstrap.admin.email}")
    private String adminEmail;

    /** Admin password for bootstrap user. */
    @Value("${accessiq.bootstrap.admin.password}")
    private String adminPassword;

    /** Sample user password. */
    @Value("${accessiq.bootstrap.sample.password}")
    private String samplePassword;

    /**
     * Constructs a new DataInitializer.
     *
     * @param roleRepository the role repository
     * @param userRepository the user repository
     * @param workflowRepository the workflow definition repository
     * @param passwordEncoder the password encoder
     */
    public DataInitializer(
            final RoleRepository roleRepository,
            final UserRepository userRepository,
            final WorkflowDefinitionRepository workflowRepository,
            final PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.workflowRepository = workflowRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Initializes the bootstrap data.
     *
     * @param args the command line arguments
     * @throws Exception if initialization fails
     */
    @Override
    public void run(final String... args) throws Exception {
        log.info("Initializing bootstrap data...");

        initializeRoles();
        initializeAdminUser();
        initializeSampleUsers();
        initializeDefaultWorkflow();

        log.info("Bootstrap data initialization complete");
    }

    /**
     * Initializes the default roles.
     */
    private void initializeRoles() {
        log.debug("Initializing roles...");
        for (final RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> {
                final Role role = new Role(roleName);
                log.info("Created role: {}", roleName);
                return roleRepository.save(role);
            });
        }
    }

    /**
     * Initializes the admin user.
     */
    private void initializeAdminUser() {
        log.debug("Initializing admin user...");
        if (!userRepository.existsByEmail(adminEmail)) {
            final User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(Set.of(roleRepository.findByName(RoleName.ADMIN).orElseThrow()));
            userRepository.save(admin);
            log.info("Created admin user: {}", adminEmail);
        } else {
            log.debug("Admin user already exists: {}", adminEmail);
        }
    }

    /**
     * Initializes the sample users.
     */
    private void initializeSampleUsers() {
        log.debug("Initializing sample users...");
        createSampleUserIfMissing("employee@accessiq.com", RoleName.EMPLOYEE);
        createSampleUserIfMissing("manager@accessiq.com", RoleName.MANAGER);
        createSampleUserIfMissing("auditor@accessiq.com", RoleName.AUDITOR);
    }

    /**
     * Creates a sample user if not existing.
     *
     * @param email the email
     * @param roleName the role name
     */
    private void createSampleUserIfMissing(final String email, final RoleName roleName) {
        if (!userRepository.existsByEmail(email)) {
            final User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(samplePassword));
            user.setRoles(Set.of(roleRepository.findByName(roleName).orElseThrow()));
            userRepository.save(user);
            log.info("Created sample user: {} with role: {}", email, roleName);
        } else {
            log.debug("Sample user already exists: {}", email);
        }
    }

    /**
     * Initializes the default workflow.
     */
    private void initializeDefaultWorkflow() {
        log.debug("Initializing default workflow...");
        if (workflowRepository.count() == 0) {
            final WorkflowDefinition workflow = new WorkflowDefinition();
            workflow.setName("DEFAULT");
            workflow.setActive(true);
            workflow.setCreatedBy("system");

            final WorkflowStepDefinition step1 = new WorkflowStepDefinition();
            step1.setWorkflow(workflow);
            step1.setApproverRole(RoleName.MANAGER);
            step1.setStepOrder(1);
            step1.setSlaHours(24);

            final WorkflowStepDefinition step2 = new WorkflowStepDefinition();
            step2.setWorkflow(workflow);
            step2.setApproverRole(RoleName.ADMIN);
            step2.setStepOrder(2);
            step2.setSlaHours(24);

            workflow.getSteps().add(step1);
            workflow.getSteps().add(step2);

            workflowRepository.save(workflow);
            log.info("Created default workflow with {} steps", workflow.getSteps().size());
        } else {
            log.debug("Default workflow already exists");
        }
    }
}