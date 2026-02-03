package com.accessiq.controller;

import com.accessiq.dto.CreateUserRequest;
import com.accessiq.dto.WorkflowCreateRequest;
import com.accessiq.model.User;
import com.accessiq.model.WorkflowDefinition;
import com.accessiq.service.AuditService;
import com.accessiq.service.UserService;
import com.accessiq.service.WorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final WorkflowService workflowService;
    private final AuditService auditService;

    public AdminController(UserService userService, WorkflowService workflowService, AuditService auditService) {
        this.userService = userService;
        this.workflowService = workflowService;
        this.auditService = auditService;
    }

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody CreateUserRequest request, Authentication authentication) {
        User user = userService.createUser(request.getEmail(), request.getPassword(), request.getRoles());
        auditService.log("USER_CREATED", authentication.getName(), "User ID: " + user.getId());
        return user;
    }

    @PostMapping("/workflows")
    public WorkflowDefinition createWorkflow(@Valid @RequestBody WorkflowCreateRequest request, Authentication authentication) {
        WorkflowDefinition workflow = workflowService.createWorkflow(request, authentication.getName());
        auditService.log("WORKFLOW_CREATED", authentication.getName(), "Workflow ID: " + workflow.getId());
        return workflow;
    }

    @PostMapping("/workflows/{id}/activate")
    public WorkflowDefinition activateWorkflow(@PathVariable Long id, Authentication authentication) {
        WorkflowDefinition workflow = workflowService.setActive(id);
        auditService.log("WORKFLOW_ACTIVATED", authentication.getName(), "Workflow ID: " + workflow.getId());
        return workflow;
    }

    @GetMapping("/workflows")
    public List<WorkflowDefinition> listWorkflows() {
        return workflowService.listWorkflows();
    }
}
