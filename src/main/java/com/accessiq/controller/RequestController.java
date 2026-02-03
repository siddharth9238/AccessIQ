package com.accessiq.controller;

import com.accessiq.dto.ApprovalDecisionRequest;
import com.accessiq.dto.RequestCreateRequest;
import com.accessiq.model.Request;
import com.accessiq.model.RoleName;
import com.accessiq.model.User;
import com.accessiq.service.RequestService;
import com.accessiq.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;
    private final UserService userService;

    public RequestController(RequestService requestService, UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public Request createRequest(@Valid @RequestBody RequestCreateRequest request, Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        return requestService.createRequest(request, user);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public void approveRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalDecisionRequest decision,
            Authentication authentication
    ) {
        User user = userService.getByEmail(authentication.getName());
        String details = decision != null ? decision.getDetails() : null;
        requestService.approveRequest(id, user, details);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public void rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) ApprovalDecisionRequest decision,
            Authentication authentication
    ) {
        User user = userService.getByEmail(authentication.getName());
        String details = decision != null ? decision.getDetails() : null;
        requestService.rejectRequest(id, user, details);
    }

    @GetMapping
    public List<Request> listRequests(Authentication authentication) {
        User user = userService.getByEmail(authentication.getName());
        boolean isPrivileged = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ADMIN
                        || role.getName() == RoleName.AUDITOR
                        || role.getName() == RoleName.MANAGER);
        if (isPrivileged) {
            return requestService.listAll();
        }
        return requestService.listForUser(user);
    }
}
