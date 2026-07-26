package com.accessiq.service;

import com.accessiq.dto.RequestCreateRequest;
import com.accessiq.exception.BadRequestException;
import com.accessiq.exception.ResourceNotFoundException;
import com.accessiq.model.ApprovalStatus;
import com.accessiq.model.ApprovalStep;
import com.accessiq.model.Request;
import com.accessiq.model.RequestStatus;
import com.accessiq.model.Role;
import com.accessiq.model.RoleName;
import com.accessiq.model.User;
import com.accessiq.model.WorkflowDefinition;
import com.accessiq.model.WorkflowStepDefinition;
import com.accessiq.repository.ApprovalStepRepository;
import com.accessiq.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ApprovalStepRepository approvalStepRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private RequestService requestService;

    private User testUser;
    private WorkflowDefinition testWorkflow;
    private WorkflowStepDefinition testStep1;
    private WorkflowStepDefinition testStep2;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testWorkflow = new WorkflowDefinition();
        testWorkflow.setId(1L);
        testWorkflow.setName("DEFAULT");
        testWorkflow.setActive(true);

        testStep1 = new WorkflowStepDefinition();
        testStep1.setId(1L);
        testStep1.setWorkflow(testWorkflow);
        testStep1.setApproverRole(RoleName.MANAGER);
        testStep1.setStepOrder(1);

        testStep2 = new WorkflowStepDefinition();
        testStep2.setId(2L);
        testStep2.setWorkflow(testWorkflow);
        testStep2.setApproverRole(RoleName.ADMIN);
        testStep2.setStepOrder(2);
    }

    @Test
    void testCreateRequestSuccess() {
        RequestCreateRequest dto = new RequestCreateRequest();
        dto.setTitle("Test Request");
        dto.setDescription("Test Description");
        dto.setWorkflowName("DEFAULT");

        Request savedRequest = new Request();
        savedRequest.setId(1L);
        savedRequest.setTitle("Test Request");
        savedRequest.setCreatedBy(testUser);
        savedRequest.setStatus(RequestStatus.PENDING);

        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);
        when(approvalStepRepository.save(any(ApprovalStep.class))).thenReturn(new ApprovalStep());
        when(workflowService.getActiveOrByName("DEFAULT")).thenReturn(testWorkflow);
        when(workflowService.getSteps(1L)).thenReturn(List.of(testStep1, testStep2));

        Request result = requestService.createRequest(dto, testUser);

        assertNotNull(result);
        assertEquals("Test Request", result.getTitle());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        verify(requestRepository).save(any(Request.class));
    }

    @Test
    void testCreateRequestWithNoWorkflow() {
        RequestCreateRequest dto = new RequestCreateRequest();
        dto.setTitle("Test Request");
        dto.setDescription("Test Description");

        Request savedRequest = new Request();
        savedRequest.setId(1L);
        savedRequest.setTitle("Test Request");
        savedRequest.setCreatedBy(testUser);

        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);
        when(approvalStepRepository.save(any(ApprovalStep.class))).thenReturn(new ApprovalStep());
        when(workflowService.getActiveOrByName(null)).thenReturn(testWorkflow);
        when(workflowService.getSteps(1L)).thenReturn(List.of(testStep1, testStep2));

        Request result = requestService.createRequest(dto, testUser);

        assertNotNull(result);
    }

    @Test
    void testApproveRequestSuccess() {
        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.PENDING);

        ApprovalStep step = new ApprovalStep();
        step.setId(1L);
        step.setStatus(ApprovalStatus.PENDING);
        step.setApproverRole(RoleName.MANAGER);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalStepRepository.findByRequestIdOrderByStepOrderAsc(1L))
            .thenReturn(List.of(step));
        when(approvalStepRepository.save(any(ApprovalStep.class))).thenReturn(step);
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        User approver = new User();
        approver.setId(2L);
        approver.setEmail("approver@example.com");
        approver.setRoles(Set.of(new Role(RoleName.MANAGER)));

        Request result = requestService.approveRequest(1L, approver, "Details");

        assertEquals(RequestStatus.APPROVED, result.getStatus());
    }

    @Test
    void testApproveRequestAlreadyApproved() {
        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.APPROVED);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        User approver = new User();
        approver.setId(2L);
        approver.setEmail("approver@example.com");
        approver.setRoles(Set.of(new Role(RoleName.MANAGER)));

        assertThrows(BadRequestException.class, () ->
            requestService.approveRequest(1L, approver, "Details")
        );
    }

    @Test
    void testGetByIdSuccess() {
        Request request = new Request();
        request.setId(1L);
        request.setTitle("Test Request");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        Request result = requestService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetByIdNotFound() {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            requestService.getById(1L)
        );
    }

    @Test
    void testRejectRequestSuccess() {
        Request request = new Request();
        request.setId(1L);
        request.setStatus(RequestStatus.PENDING);

        ApprovalStep step = new ApprovalStep();
        step.setId(1L);
        step.setStatus(ApprovalStatus.PENDING);
        step.setApproverRole(RoleName.MANAGER);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(approvalStepRepository.findByRequestIdOrderByStepOrderAsc(1L))
            .thenReturn(List.of(step));
        when(approvalStepRepository.save(any(ApprovalStep.class))).thenReturn(step);
        when(requestRepository.save(any(Request.class))).thenReturn(request);

        User approver = new User();
        approver.setId(2L);
        approver.setEmail("approver@example.com");
        approver.setRoles(Set.of(new Role(RoleName.MANAGER)));

        Request result = requestService.rejectRequest(1L, approver, "Details");

        assertEquals(RequestStatus.REJECTED, result.getStatus());
    }
}