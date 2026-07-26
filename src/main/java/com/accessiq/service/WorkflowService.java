package com.accessiq.service;

import com.accessiq.dto.WorkflowCreateRequest;
import com.accessiq.dto.WorkflowResponse;
import com.accessiq.dto.WorkflowStepRequest;
import com.accessiq.dto.WorkflowStepResponse;
import com.accessiq.exception.BadRequestException;
import com.accessiq.exception.ResourceNotFoundException;
import com.accessiq.model.WorkflowDefinition;
import com.accessiq.model.WorkflowStepDefinition;
import com.accessiq.repository.WorkflowDefinitionRepository;
import com.accessiq.repository.WorkflowStepDefinitionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for workflow management operations.
 */
@Service
@Transactional(readOnly = true)
public class WorkflowService {

    /** Logger for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(WorkflowService.class);

    /** The workflow definition repository. */
    private final WorkflowDefinitionRepository workflowRepository;

    /** The workflow step definition repository. */
    private final WorkflowStepDefinitionRepository stepRepository;

    /**
     * Constructs a new WorkflowService.
     *
     * @param workflowRepository the workflow definition repository
     * @param stepRepository the workflow step definition repository
     */
    public WorkflowService(final WorkflowDefinitionRepository workflowRepository,
            final WorkflowStepDefinitionRepository stepRepository) {
        this.workflowRepository = workflowRepository;
        this.stepRepository = stepRepository;
    }

    /**
     * Creates a new workflow.
     *
     * @param request the workflow create request
     * @param createdBy the creator email
     * @return the created workflow
     */
    @Transactional
    public WorkflowDefinition createWorkflow(final WorkflowCreateRequest request,
            final String createdBy) {
        LOG.info("Creating workflow with name: {}", request.getName());

        workflowRepository.findByName(request.getName()).ifPresent(existing -> {
            throw new BadRequestException("Workflow already exists: " + existing.getName());
        });

        final WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setName(request.getName());
        workflow.setActive(true);
        workflow.setCreatedBy(createdBy);

        request.getSteps().stream()
                .sorted(Comparator.comparingInt(WorkflowStepRequest::getStepOrder))
                .forEach(stepRequest -> {
                    final WorkflowStepDefinition step = new WorkflowStepDefinition();
                    step.setWorkflow(workflow);
                    step.setApproverRole(stepRequest.getApproverRole());
                    step.setStepOrder(stepRequest.getStepOrder());
                    step.setSlaHours(stepRequest.getSlaHours());
                    workflow.getSteps().add(step);
                });

        final WorkflowDefinition savedWorkflow = workflowRepository.save(workflow);
        LOG.info("Workflow created successfully with ID: {}", savedWorkflow.getId());
        return savedWorkflow;
    }

    /**
     * Gets the active workflow or by name.
     *
     * @param name the workflow name
     * @return the workflow
     */
    @Transactional(readOnly = true)
    public WorkflowDefinition getActiveOrByName(final String name) {
        if (name != null && !name.isBlank()) {
            return workflowRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Workflow not found: " + name));
        }
        return workflowRepository.findFirstByActiveTrueOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active workflow found"));
    }

    /**
     * Gets a workflow by ID.
     *
     * @param id the workflow ID
     * @return the workflow
     */
    @Transactional(readOnly = true)
    public WorkflowDefinition getById(final Long id) {
        return workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow not found: " + id));
    }

    /**
     * Lists all workflows.
     *
     * @return the list of workflows
     */
    @Transactional(readOnly = true)
    public List<WorkflowDefinition> listWorkflows() {
        return workflowRepository.findAll();
    }

    /**
     * Lists all workflows with pagination.
     *
     * @param pageable the pageable
     * @return the page of workflows
     */
    @Transactional(readOnly = true)
    public Page<WorkflowDefinition> listWorkflows(final Pageable pageable) {
        return workflowRepository.findAll(pageable);
    }

    /**
     * Gets steps for a workflow.
     *
     * @param workflowId the workflow ID
     * @return the list of workflow steps
     */
    @Transactional(readOnly = true)
    public List<WorkflowStepDefinition> getSteps(final Long workflowId) {
        return stepRepository.findByWorkflowIdOrderByStepOrderAsc(workflowId);
    }

    /**
     * Sets a workflow as active.
     *
     * @param workflowId the workflow ID
     * @return the updated workflow
     */
    @Transactional
    public WorkflowDefinition setActive(final Long workflowId) {
        LOG.info("Setting workflow {} as active", workflowId);

        final WorkflowDefinition workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workflow not found: " + workflowId));

        workflowRepository.findAll().forEach(wf -> {
            if (wf.isActive() && !wf.getId().equals(workflowId)) {
                wf.setActive(false);
                workflowRepository.save(wf);
            }
        });

        workflow.setActive(true);
        final WorkflowDefinition savedWorkflow = workflowRepository.save(workflow);
        LOG.info("Workflow {} set as active", savedWorkflow.getId());
        return savedWorkflow;
    }

    /**
     * Updates a workflow.
     *
     * @param id the workflow ID
     * @param request the workflow create request
     * @return the updated workflow
     */
    @Transactional
    public WorkflowDefinition updateWorkflow(final Long id,
            final WorkflowCreateRequest request) {
        LOG.info("Updating workflow with ID: {}", id);

        final WorkflowDefinition existing = getById(id);
        existing.setName(request.getName());

        existing.getSteps().clear();
        request.getSteps().stream()
                .sorted(Comparator.comparingInt(WorkflowStepRequest::getStepOrder))
                .forEach(stepRequest -> {
                    final WorkflowStepDefinition stepDef = new WorkflowStepDefinition();
                    stepDef.setWorkflow(existing);
                    stepDef.setApproverRole(stepRequest.getApproverRole());
                    stepDef.setStepOrder(stepRequest.getStepOrder());
                    stepDef.setSlaHours(stepRequest.getSlaHours());
                    existing.getSteps().add(stepDef);
                });

        final WorkflowDefinition savedWorkflow = workflowRepository.save(existing);
        LOG.info("Workflow updated successfully with ID: {}",
                savedWorkflow.getId());
        return savedWorkflow;
    }

    /**
     * Deletes a workflow.
     *
     * @param id the workflow ID
     */
    @Transactional
    public void deleteWorkflow(final Long id) {
        LOG.info("Deleting workflow with ID: {}", id);

        final WorkflowDefinition workflow = getById(id);
        workflowRepository.delete(workflow);
        LOG.info("Workflow deleted successfully with ID: {}", id);
    }

    /**
     * Converts a workflow to a response.
     *
     * @param workflow the workflow
     * @return the workflow response
     */
    @Transactional(readOnly = true)
    public WorkflowResponse toWorkflowResponse(final WorkflowDefinition workflow) {
        final List<WorkflowStepResponse> steps = workflow.getSteps().stream()
                .map(this::toWorkflowStepResponse)
                .collect(Collectors.toList());

        return new WorkflowResponse(
                workflow.getId(),
                workflow.getName(),
                workflow.isActive(),
                workflow.getCreatedBy(),
                workflow.getCreatedAt(),
                steps
        );
    }

    /**
     * Converts a workflow step to a response.
     *
     * @param step the workflow step
     * @return the workflow step response
     */
    public WorkflowStepResponse toWorkflowStepResponse(
            final WorkflowStepDefinition step) {
        return new WorkflowStepResponse(
                step.getId(),
                step.getApproverRole(),
                step.getStepOrder(),
                step.getSlaHours()
        );
    }
}