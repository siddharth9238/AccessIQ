package com.accessiq.service;

import com.accessiq.dto.RequestCreateRequest;
import com.accessiq.exception.BadRequestException;
import com.accessiq.exception.ResourceNotFoundException;
import com.accessiq.model.ApprovalStatus;
import com.accessiq.model.ApprovalStep;
import com.accessiq.model.Request;
import com.accessiq.model.RequestStatus;
import com.accessiq.model.RoleName;
import com.accessiq.model.User;
import com.accessiq.model.WorkflowDefinition;
import com.accessiq.model.WorkflowStepDefinition;
import com.accessiq.repository.ApprovalStepRepository;
import com.accessiq.repository.RequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final ApprovalStepRepository approvalStepRepository;
    private final WorkflowService workflowService;
    private final AuditService auditService;

    public RequestService(
            RequestRepository requestRepository,
            ApprovalStepRepository approvalStepRepository,
            WorkflowService workflowService,
            AuditService auditService
    ) {
        this.requestRepository = requestRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.workflowService = workflowService;
        this.auditService = auditService;
    }

    public Request createRequest(RequestCreateRequest dto, User user) {
        Request request = new Request();
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedBy(user);

        request = requestRepository.save(request);

        WorkflowDefinition workflow = workflowService.getActiveOrByName(dto.getWorkflowName());
        List<WorkflowStepDefinition> steps = workflowService.getSteps(workflow.getId());
        if (steps.isEmpty()) {
            throw new BadRequestException("Workflow has no steps");
        }
        createApprovalSteps(request, steps);

        auditService.log("REQUEST_CREATED", user.getEmail(), "Request ID: " + request.getId());
        return request;
    }

    @Transactional
    public void approveRequest(Long requestId, User approver, String details) {
        Request request = getRequestOrThrow(requestId);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Request is already " + request.getStatus());
        }

        ApprovalStep step = getNextPendingStepForRole(requestId, approver);
        step.setStatus(ApprovalStatus.APPROVED);
        step.setDecidedBy(approver);
        step.setDecidedAt(LocalDateTime.now());
        approvalStepRepository.save(step);

        if (isLastStepApproved(requestId)) {
            request.setStatus(RequestStatus.APPROVED);
            requestRepository.save(request);
        }

        auditService.log("REQUEST_APPROVED", approver.getEmail(),
                "Request ID: " + requestId + (details != null ? " | " + details : ""));
    }

    @Transactional
    public void rejectRequest(Long requestId, User approver, String details) {
        Request request = getRequestOrThrow(requestId);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Request is already " + request.getStatus());
        }

        ApprovalStep step = getNextPendingStepForRole(requestId, approver);
        step.setStatus(ApprovalStatus.REJECTED);
        step.setDecidedBy(approver);
        step.setDecidedAt(LocalDateTime.now());
        approvalStepRepository.save(step);

        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        auditService.log("REQUEST_REJECTED", approver.getEmail(),
                "Request ID: " + requestId + (details != null ? " | " + details : ""));
    }

    public List<Request> listAll() {
        return requestRepository.findAll();
    }

    public List<Request> listForUser(User user) {
        return requestRepository.findByCreatedById(user.getId());
    }

    private void createApprovalSteps(Request request, List<WorkflowStepDefinition> steps) {
        for (WorkflowStepDefinition def : steps) {
            ApprovalStep step = new ApprovalStep();
            step.setRequest(request);
            step.setApproverRole(def.getApproverRole());
            step.setStepOrder(def.getStepOrder());
            step.setStatus(ApprovalStatus.PENDING);
            if (def.getSlaHours() != null) {
                step.setSlaDueAt(LocalDateTime.now().plusHours(def.getSlaHours()));
            }
            approvalStepRepository.save(step);
            request.getApprovalSteps().add(step);
        }
    }

    private ApprovalStep getNextPendingStepForRole(Long requestId, User approver) {
        List<RoleName> roles = approver.getRoles().stream()
                .map(r -> r.getName())
                .toList();
        if (roles.isEmpty()) {
            throw new BadRequestException("Approver has no role");
        }

        List<ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByStepOrderAsc(requestId);
        ApprovalStep next = steps.stream()
                .filter(step -> step.getStatus() == ApprovalStatus.PENDING || step.getStatus() == ApprovalStatus.ESCALATED)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No pending approval steps"));

        if (!roles.contains(next.getApproverRole())) {
            throw new BadRequestException("Current step requires role " + next.getApproverRole());
        }

        return next;
    }

    private boolean isLastStepApproved(Long requestId) {
        List<ApprovalStep> steps = approvalStepRepository.findByRequestIdOrderByStepOrderAsc(requestId);
        return steps.stream().noneMatch(step ->
                step.getStatus() == ApprovalStatus.PENDING || step.getStatus() == ApprovalStatus.ESCALATED);
    }

    private Request getRequestOrThrow(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
    }
}
