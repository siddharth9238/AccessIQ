package com.accessiq.service;

import com.accessiq.dto.ApprovalStepResponse;
import com.accessiq.dto.RequestCreateRequest;
import com.accessiq.dto.RequestResponse;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for request management operations.
 */
@Service
public class RequestService {

    /** Logger for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(RequestService.class);

    /** The request repository. */
    private final RequestRepository requestRepository;

    /** The approval step repository. */
    private final ApprovalStepRepository approvalStepRepository;

    /** The workflow service. */
    private final WorkflowService workflowService;

    /** The audit service. */
    private final AuditService auditService;

    /**
     * Constructs a new RequestService.
     *
     * @param requestRepository the request repository
     * @param approvalStepRepository the approval step repository
     * @param workflowService the workflow service
     * @param auditService the audit service
     */
    public RequestService(final RequestRepository requestRepository,
            final ApprovalStepRepository approvalStepRepository,
            final WorkflowService workflowService,
            final AuditService auditService) {
        this.requestRepository = requestRepository;
        this.approvalStepRepository = approvalStepRepository;
        this.workflowService = workflowService;
        this.auditService = auditService;
    }

    /**
     * Creates a new request.
     *
     * @param dto the request create request
     * @param user the user creating the request
     * @return the created request
     */
    @Transactional
    public Request createRequest(final RequestCreateRequest dto, final User user) {
        LOG.info("Creating request with title: {} for user: {}",
                dto.getTitle(), user.getEmail());

        final Request request = new Request();
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedBy(user);

        final Request savedRequest = requestRepository.save(request);
        LOG.debug("Request saved with ID: {}", savedRequest.getId());

        WorkflowDefinition workflow;
        if (dto.getWorkflowName() != null && !dto.getWorkflowName().isBlank()) {
            workflow = workflowService.getActiveOrByName(dto.getWorkflowName());
        } else {
            workflow = workflowService.getActiveOrByName(null);
        }

        final List<WorkflowStepDefinition> steps = workflowService
                .getSteps(workflow.getId());
        if (steps.isEmpty()) {
            throw new BadRequestException("Workflow has no steps");
        }
        createApprovalSteps(savedRequest, steps);

        auditService.log("REQUEST_CREATED", user.getEmail(),
                "Request ID: " + savedRequest.getId());
        LOG.info("Request created successfully with ID: {}", savedRequest.getId());
        return savedRequest;
    }

    /**
     * Approves a request.
     *
     * @param requestId the request ID
     * @param approver the approver
     * @param details the approval details
     * @return the updated request
     */
    @Transactional
    public Request approveRequest(final Long requestId, final User approver,
            final String details) {
        LOG.info("Approving request ID: {} by user: {}",
                requestId, approver.getEmail());

        final Request request = getRequestOrThrow(requestId);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Request is already " + request.getStatus());
        }

        final ApprovalStep step = getNextPendingStepForRole(requestId, approver);
        step.setStatus(ApprovalStatus.APPROVED);
        step.setDecidedBy(approver);
        step.setDecidedAt(LocalDateTime.now());
        approvalStepRepository.save(step);

        if (isLastStepApproved(requestId)) {
            request.setStatus(RequestStatus.APPROVED);
            requestRepository.save(request);
            LOG.info("Request {} fully approved", requestId);
        }

        auditService.log("REQUEST_APPROVED", approver.getEmail(),
                "Request ID: " + requestId
                        + (details != null ? " | " + details : ""));

        return request;
    }

    /**
     * Rejects a request.
     *
     * @param requestId the request ID
     * @param approver the approver
     * @param details the rejection details
     * @return the updated request
     */
    @Transactional
    public Request rejectRequest(final Long requestId, final User approver,
            final String details) {
        LOG.info("Rejecting request ID: {} by user: {}",
                requestId, approver.getEmail());

        final Request request = getRequestOrThrow(requestId);
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Request is already " + request.getStatus());
        }

        final ApprovalStep step = getNextPendingStepForRole(requestId, approver);
        step.setStatus(ApprovalStatus.REJECTED);
        step.setDecidedBy(approver);
        step.setDecidedAt(LocalDateTime.now());
        approvalStepRepository.save(step);

        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        auditService.log("REQUEST_REJECTED", approver.getEmail(),
                "Request ID: " + requestId
                        + (details != null ? " | " + details : ""));

        return request;
    }

    /**
     * Gets a request by ID.
     *
     * @param id the request ID
     * @return the request
     */
    @Transactional(readOnly = true)
    public Request getById(final Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Request not found: " + id));
    }

    /**
     * Lists all requests.
     *
     * @return the list of requests
     */
    @Transactional(readOnly = true)
    public List<Request> listAll() {
        return requestRepository.findAll();
    }

    /**
     * Lists all requests with pagination.
     *
     * @param pageable the pageable
     * @return the page of requests
     */
    @Transactional(readOnly = true)
    public Page<Request> listAll(final Pageable pageable) {
        return requestRepository.findAll(pageable);
    }

    /**
     * Lists requests for a user.
     *
     * @param user the user
     * @return the list of requests
     */
    @Transactional(readOnly = true)
    public List<Request> listForUser(final User user) {
        return requestRepository.findByCreatedById(user.getId());
    }

    /**
     * Lists requests for a user with pagination.
     *
     * @param user the user
     * @param pageable the pageable
     * @return the page of requests
     */
    @Transactional(readOnly = true)
    public Page<Request> listForUser(final User user, final Pageable pageable) {
        return requestRepository.findByCreatedById(user.getId(), pageable);
    }

    /**
     * Lists requests by status.
     *
     * @param status the status
     * @return the list of requests
     */
    @Transactional(readOnly = true)
    public List<Request> listByStatus(final RequestStatus status) {
        return requestRepository.findByStatus(status);
    }

    /**
     * Lists requests by status with pagination.
     *
     * @param status the status
     * @param pageable the pageable
     * @return the page of requests
     */
    @Transactional(readOnly = true)
    public Page<Request> listByStatus(final RequestStatus status,
            final Pageable pageable) {
        return requestRepository.findByStatus(status, pageable);
    }

    /**
     * Gets request responses for a user.
     *
     * @param user the user
     * @return the list of request responses
     */
    @Transactional(readOnly = true)
    public List<RequestResponse> getRequestResponses(final User user) {
        final boolean isPrivileged = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.ADMIN
                        || role.getName() == RoleName.AUDITOR
                        || role.getName() == RoleName.MANAGER);

        final List<Request> requests = isPrivileged ? listAll() : listForUser(user);
        return requests.stream()
                .map(this::toRequestResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets a request response by ID.
     *
     * @param id the request ID
     * @return the request response
     */
    @Transactional(readOnly = true)
    public RequestResponse getRequestResponse(final Long id) {
        final Request request = getById(id);
        return toRequestResponse(request);
    }

    /**
     * Converts a request to a request response.
     *
     * @param request the request
     * @return the request response
     */
    public RequestResponse toRequestResponse(final Request request) {
        final List<ApprovalStepResponse> steps = request.getApprovalSteps().stream()
                .map(this::toApprovalStepResponse)
                .collect(Collectors.toList());

        return new RequestResponse(
                request.getId(),
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getCreatedBy() != null ? request.getCreatedBy().getEmail() : null,
                request.getCreatedAt(),
                steps
        );
    }

    /**
     * Converts an approval step to a response.
     *
     * @param step the approval step
     * @return the approval step response
     */
    public ApprovalStepResponse toApprovalStepResponse(final ApprovalStep step) {
        return new ApprovalStepResponse(
                step.getId(),
                step.getApproverRole().name(),
                step.getStepOrder(),
                step.getStatus(),
                step.getDecidedBy() != null ? step.getDecidedBy().getEmail() : null,
                step.getDecidedAt(),
                step.getSlaDueAt()
        );
    }

    /**
     * Creates approval steps for a request.
     *
     * @param request the request
     * @param steps the workflow steps
     */
    private void createApprovalSteps(final Request request,
            final List<WorkflowStepDefinition> steps) {
        for (final WorkflowStepDefinition def : steps) {
            final ApprovalStep step = new ApprovalStep();
            step.setRequest(request);
            step.setApproverRole(def.getApproverRole());
            step.setStepOrder(def.getStepOrder());
            step.setStatus(ApprovalStatus.PENDING);
            if (def.getSlaHours() != null) {
                step.setSlaDueAt(LocalDateTime.now().plusHours(
                        def.getSlaHours()));
            }
            approvalStepRepository.save(step);
            request.getApprovalSteps().add(step);
        }
    }

    /**
     * Gets the next pending step for a role.
     *
     * @param requestId the request ID
     * @param approver the approver
     * @return the approval step
     */
    private ApprovalStep getNextPendingStepForRole(final Long requestId,
            final User approver) {
        final List<RoleName> roles = approver.getRoles().stream()
                .map(r -> r.getName())
                .toList();
        if (roles.isEmpty()) {
            throw new BadRequestException("Approver has no role");
        }

        final List<ApprovalStep> steps = approvalStepRepository
                .findByRequestIdOrderByStepOrderAsc(requestId);
        final ApprovalStep next = steps.stream()
                .filter(step -> step.getStatus() == ApprovalStatus.PENDING
                        || step.getStatus() == ApprovalStatus.ESCALATED)
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "No pending approval steps"));

        if (!roles.contains(next.getApproverRole())) {
            throw new BadRequestException(
                    "Current step requires role " + next.getApproverRole());
        }

        return next;
    }

    /**
     * Checks if the last step is approved.
     *
     * @param requestId the request ID
     * @return true if last step is approved
     */
    private boolean isLastStepApproved(final Long requestId) {
        final List<ApprovalStep> steps = approvalStepRepository
                .findByRequestIdOrderByStepOrderAsc(requestId);
        return steps.stream().noneMatch(step ->
                step.getStatus() == ApprovalStatus.PENDING
                        || step.getStatus() == ApprovalStatus.ESCALATED);
    }

    /**
     * Gets a request or throws an exception.
     *
     * @param requestId the request ID
     * @return the request
     */
    private Request getRequestOrThrow(final Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Request not found: " + requestId));
    }
}