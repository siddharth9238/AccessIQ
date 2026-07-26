package com.accessiq.controller;

import com.accessiq.dto.ApiResponse;
import com.accessiq.dto.CreateUserRequest;
import com.accessiq.dto.UserResponse;
import com.accessiq.dto.WorkflowCreateRequest;
import com.accessiq.dto.WorkflowResponse;
import com.accessiq.model.User;
import com.accessiq.model.WorkflowDefinition;
import com.accessiq.service.AuditService;
import com.accessiq.service.UserService;
import com.accessiq.service.WorkflowService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;
    private final WorkflowService workflowService;
    private final AuditService auditService;

    public AdminController(UserService userService, WorkflowService workflowService, AuditService auditService) {
        this.userService = userService;
        this.workflowService = workflowService;
        this.auditService = auditService;
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication
    ) {
        logger.info("Creating user: {} by admin: {}", request.getEmail(), authentication.getName());
        User user = userService.createUser(request.getEmail(), request.getPassword(), request.getRoles());
        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(java.util.stream.Collectors.toSet())
        );
        auditService.log("USER_CREATED", authentication.getName(), "User ID: " + user.getId());
        return ResponseEntity.ok(ApiResponse.success("User created successfully", response));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> listUsers() {
        logger.info("Listing all users");
        List<UserResponse> users = userService.getAllUserResponses(Pageable.unpaged())
                .getContent();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/page")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsersPaged(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        logger.info("Listing paged users");
        Page<UserResponse> users = userService.getAllUserResponses(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        logger.info("Getting user with ID: {}", id);
        User user = userService.getById(id);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(java.util.stream.Collectors.toSet())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication
    ) {
        logger.info("Updating user ID: {} by admin: {}", id, authentication.getName());
        User user = userService.updateUser(id, request.getEmail(), request.getRoles());
        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(java.util.stream.Collectors.toSet())
        );
        auditService.log("USER_UPDATED", authentication.getName(), "User ID: " + user.getId());
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        logger.info("Deleting user ID: {} by admin: {}", id, authentication.getName());
        userService.deleteUser(id);
        auditService.log("USER_DELETED", authentication.getName(), "User ID: " + id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PostMapping("/workflows")
    public ResponseEntity<ApiResponse<WorkflowResponse>> createWorkflow(
            @Valid @RequestBody WorkflowCreateRequest request,
            Authentication authentication
    ) {
        logger.info("Creating workflow: {} by admin: {}", request.getName(), authentication.getName());
        WorkflowDefinition workflow = workflowService.createWorkflow(request, authentication.getName());
        WorkflowResponse response = workflowService.toWorkflowResponse(workflow);
        auditService.log("WORKFLOW_CREATED", authentication.getName(), "Workflow ID: " + workflow.getId());
        return ResponseEntity.ok(ApiResponse.success("Workflow created successfully", response));
    }

    @GetMapping("/workflows")
    public ResponseEntity<ApiResponse<List<WorkflowResponse>>> listWorkflows() {
        logger.info("Listing all workflows");
        List<WorkflowDefinition> workflows = workflowService.listWorkflows();
        List<WorkflowResponse> responses = workflows.stream()
                .map(workflowService::toWorkflowResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/workflows/page")
    public ResponseEntity<ApiResponse<Page<WorkflowResponse>>> listWorkflowsPaged(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        logger.info("Listing paged workflows");
        Page<WorkflowDefinition> workflows = workflowService.listWorkflows(pageable);
        Page<WorkflowResponse> responses = workflows.map(workflowService::toWorkflowResponse);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/workflows/{id}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> getWorkflow(@PathVariable Long id) {
        logger.info("Getting workflow with ID: {}", id);
        WorkflowDefinition workflow = workflowService.getById(id);
        WorkflowResponse response = workflowService.toWorkflowResponse(workflow);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/workflows/{id}/activate")
    public ResponseEntity<ApiResponse<WorkflowResponse>> activateWorkflow(
            @PathVariable Long id,
            Authentication authentication
    ) {
        logger.info("Activating workflow ID: {} by admin: {}", id, authentication.getName());
        WorkflowDefinition workflow = workflowService.setActive(id);
        WorkflowResponse response = workflowService.toWorkflowResponse(workflow);
        auditService.log("WORKFLOW_ACTIVATED", authentication.getName(), "Workflow ID: " + workflow.getId());
        return ResponseEntity.ok(ApiResponse.success("Workflow activated successfully", response));
    }

    @PutMapping("/workflows/{id}")
    public ResponseEntity<ApiResponse<WorkflowResponse>> updateWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowCreateRequest request,
            Authentication authentication
    ) {
        logger.info("Updating workflow ID: {} by admin: {}", id, authentication.getName());
        WorkflowDefinition workflow = workflowService.updateWorkflow(id, request);
        WorkflowResponse response = workflowService.toWorkflowResponse(workflow);
        auditService.log("WORKFLOW_UPDATED", authentication.getName(), "Workflow ID: " + workflow.getId());
        return ResponseEntity.ok(ApiResponse.success("Workflow updated successfully", response));
    }

    @DeleteMapping("/workflows/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkflow(
            @PathVariable Long id,
            Authentication authentication
    ) {
        logger.info("Deleting workflow ID: {} by admin: {}", id, authentication.getName());
        workflowService.deleteWorkflow(id);
        auditService.log("WORKFLOW_DELETED", authentication.getName(), "Workflow ID: " + id);
        return ResponseEntity.ok(ApiResponse.success("Workflow deleted successfully"));
    }
}