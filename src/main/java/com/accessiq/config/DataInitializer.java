package com.accessiq.config;

import com.accessiq.model.Role;
import com.accessiq.model.RoleName;
import com.accessiq.model.User;
import com.accessiq.model.WorkflowDefinition;
import com.accessiq.model.WorkflowStepDefinition;
import com.accessiq.repository.RoleRepository;
import com.accessiq.repository.UserRepository;
import com.accessiq.repository.WorkflowDefinitionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final WorkflowDefinitionRepository workflowRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${accessiq.bootstrap.admin.email}")
    private String adminEmail;

    @Value("${accessiq.bootstrap.admin.password}")
    private String adminPassword;

    @Value("${accessiq.bootstrap.sample.password:password123}")
    private String samplePassword;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            WorkflowDefinitionRepository workflowRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.workflowRepository = workflowRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
        }

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(Set.of(roleRepository.findByName(RoleName.ADMIN).orElseThrow()));
            userRepository.save(admin);
        }

        createSampleUserIfMissing("employee@accessiq.com", RoleName.EMPLOYEE);
        createSampleUserIfMissing("manager@accessiq.com", RoleName.MANAGER);
        createSampleUserIfMissing("auditor@accessiq.com", RoleName.AUDITOR);

        if (workflowRepository.count() == 0) {
            WorkflowDefinition workflow = new WorkflowDefinition();
            workflow.setName("DEFAULT");
            workflow.setActive(true);
            workflow.setCreatedBy("system");

            WorkflowStepDefinition step1 = new WorkflowStepDefinition();
            step1.setWorkflow(workflow);
            step1.setApproverRole(RoleName.MANAGER);
            step1.setStepOrder(1);
            step1.setSlaHours(24);

            WorkflowStepDefinition step2 = new WorkflowStepDefinition();
            step2.setWorkflow(workflow);
            step2.setApproverRole(RoleName.ADMIN);
            step2.setStepOrder(2);
            step2.setSlaHours(24);

            workflow.getSteps().add(step1);
            workflow.getSteps().add(step2);

            workflowRepository.save(workflow);
        }
    }

    private void createSampleUserIfMissing(String email, RoleName roleName) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(samplePassword));
            user.setRoles(Set.of(roleRepository.findByName(roleName).orElseThrow()));
            userRepository.save(user);
        }
    }
}
