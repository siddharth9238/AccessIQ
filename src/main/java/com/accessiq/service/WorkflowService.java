package com.accessiq.service;

import com.accessiq.dto.WorkflowCreateRequest;
import com.accessiq.dto.WorkflowStepRequest;
import com.accessiq.exception.BadRequestException;
import com.accessiq.exception.ResourceNotFoundException;
import com.accessiq.model.WorkflowDefinition;
import com.accessiq.model.WorkflowStepDefinition;
import com.accessiq.repository.WorkflowDefinitionRepository;
import com.accessiq.repository.WorkflowStepDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class WorkflowService {

    private final WorkflowDefinitionRepository workflowRepository;
    private final WorkflowStepDefinitionRepository stepRepository;

    public WorkflowService(WorkflowDefinitionRepository workflowRepository, WorkflowStepDefinitionRepository stepRepository) {
        this.workflowRepository = workflowRepository;
        this.stepRepository = stepRepository;
    }

    public WorkflowDefinition createWorkflow(WorkflowCreateRequest request, String createdBy) {
        workflowRepository.findByName(request.getName()).ifPresent(existing -> {
            throw new BadRequestException("Workflow already exists: " + existing.getName());
        });

        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setName(request.getName());
        workflow.setActive(true);
        workflow.setCreatedBy(createdBy);

        request.getSteps().stream()
                .sorted(Comparator.comparingInt(WorkflowStepRequest::getStepOrder))
                .forEach(stepRequest -> {
                    WorkflowStepDefinition step = new WorkflowStepDefinition();
                    step.setWorkflow(workflow);
                    step.setApproverRole(stepRequest.getApproverRole());
                    step.setStepOrder(stepRequest.getStepOrder());
                    step.setSlaHours(stepRequest.getSlaHours());
                    workflow.getSteps().add(step);
                });

        return workflowRepository.save(workflow);
    }

    public List<WorkflowDefinition> listWorkflows() {
        return workflowRepository.findAll();
    }

    public WorkflowDefinition getActiveOrByName(String name) {
        if (name != null && !name.isBlank()) {
            return workflowRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + name));
        }
        return workflowRepository.findFirstByActiveTrueOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("No active workflow found"));
    }

    public List<WorkflowStepDefinition> getSteps(Long workflowId) {
        return stepRepository.findByWorkflowIdOrderByStepOrderAsc(workflowId);
    }

    public WorkflowDefinition setActive(Long workflowId) {
        WorkflowDefinition workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + workflowId));
        workflowRepository.findAll().forEach(wf -> {
            if (wf.isActive() && !wf.getId().equals(workflowId)) {
                wf.setActive(false);
                workflowRepository.save(wf);
            }
        });
        workflow.setActive(true);
        return workflowRepository.save(workflow);
    }
}
