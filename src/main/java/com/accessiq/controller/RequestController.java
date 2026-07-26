package com.accessiq.controller;

import com.accessiq.dto.ApiResponse;
import com.accessiq.dto.ApprovalDecisionRequest;
import com.accessiq.dto.RequestResponse;
import com.accessiq.dto.RequestCreateRequest;
import com.accessiq.model.Request;
import com.accessiq.model.RequestStatus;
import com.accessiq.model.User;
import com.accessiq.service.RequestService;
import com.accessiq.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for request management operations.
 */
@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    /** Logger for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(RequestController.class);

    /** The request service. */
    private final RequestService requestService;

    /** The user service. */
    private final UserService userService;

    /**
     * Constructs a new RequestController.
     *
     * @param requestService the request service
     * @param userService the user service
     */
    public RequestController(final RequestService requestService,
            final UserService userService) {
        this.requestService = requestService;
        this.userService = userService;
    }

    /**
     * Creates a new request.
     *
     * @param request the request create request
     * @param authentication the authentication
     * @return the API response
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<RequestResponse>> createRequest(
            @Valid @RequestBody final RequestCreateRequest request,
            final Authentication authentication
    ) {
        LOG.info("Creating request from user: {}", authentication.getName());
        final User user = userService.getByEmail(authentication.getName());
        final Request savedRequest = requestService.createRequest(request, user);
        final RequestResponse response = requestService.getRequestResponse(savedRequest.getId());
        return ResponseEntity.ok(ApiResponse.success("Request created successfully", response));
    }

    /**
     * Lists requests for the authenticated user.
     *
     * @param authentication the authentication
     * @param pageable the pageable
     * @return the API response
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RequestResponse>>> listRequests(
            final Authentication authentication,
            @PageableDefault(size = 20) final Pageable pageable
    ) {
        LOG.info("Listing requests for user: {}", authentication.getName());
        final User user = userService.getByEmail(authentication.getName());
        final List<RequestResponse> requests = requestService.getRequestResponses(user);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    /**
     * Lists paged requests for the authenticated user.
     *
     * @param authentication the authentication
     * @param pageable the pageable
     * @return the API response
     */
    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<RequestResponse>>> listRequestsPaged(
            final Authentication authentication,
            @PageableDefault(size = 20) final Pageable pageable
    ) {
        LOG.info("Listing paged requests for user: {}", authentication.getName());
        final User user = userService.getByEmail(authentication.getName());
        final Page<Request> requestPage = requestService.listForUser(user, pageable);
        final Page<RequestResponse> responsePage = requestPage.map(requestService::toRequestResponse);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * Gets a request by ID.
     *
     * @param id the request ID
     * @param authentication the authentication
     * @return the API response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RequestResponse>> getRequest(
            @PathVariable final Long id,
            final Authentication authentication
    ) {
        LOG.info("Getting request with ID: {}", id);
        final RequestResponse response = requestService.getRequestResponse(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Searches requests by keyword.
     *
     * @param keyword the search keyword
     * @param authentication the authentication
     * @return the API response
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RequestResponse>>> searchRequests(
            @RequestParam final String keyword,
            final Authentication authentication
    ) {
        LOG.info("Searching requests with keyword: {}", keyword);
        final User user = userService.getByEmail(authentication.getName());
        final List<Request> requests = requestService.listAll();
        final List<RequestResponse> filtered = requests.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || (r.getDescription() != null
                                && r.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .map(requestService::toRequestResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(filtered));
    }

    /**
     * Gets requests by status.
     *
     * @param status the status
     * @param authentication the authentication
     * @return the API response
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<ApiResponse<List<RequestResponse>>> getRequestsByStatus(
            @PathVariable final RequestStatus status,
            final Authentication authentication
    ) {
        LOG.info("Getting requests by status: {}", status);
        final List<Request> requests = requestService.listByStatus(status);
        final List<RequestResponse> responses = requests.stream()
                .map(requestService::toRequestResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Approves a request.
     *
     * @param id the request ID
     * @param decision the approval decision
     * @param authentication the authentication
     * @return the API response
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<RequestResponse>> approveRequest(
            @PathVariable final Long id,
            @RequestBody(required = false) final ApprovalDecisionRequest decision,
            final Authentication authentication
    ) {
        LOG.info("Approving request ID: {} by user: {}", id, authentication.getName());
        final User user = userService.getByEmail(authentication.getName());
        final Request request = requestService.approveRequest(id, user,
                decision != null ? decision.getDetails() : null);
        final RequestResponse response = requestService.getRequestResponse(id);
        return ResponseEntity.ok(ApiResponse.success("Request approved successfully", response));
    }

    /**
     * Rejects a request.
     *
     * @param id the request ID
     * @param decision the rejection decision
     * @param authentication the authentication
     * @return the API response
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<RequestResponse>> rejectRequest(
            @PathVariable final Long id,
            @RequestBody(required = false) final ApprovalDecisionRequest decision,
            final Authentication authentication
    ) {
        LOG.info("Rejecting request ID: {} by user: {}", id, authentication.getName());
        final User user = userService.getByEmail(authentication.getName());
        final Request request = requestService.rejectRequest(id, user,
                decision != null ? decision.getDetails() : null);
        final RequestResponse response = requestService.getRequestResponse(id);
        return ResponseEntity.ok(ApiResponse.success("Request rejected successfully", response));
    }
}