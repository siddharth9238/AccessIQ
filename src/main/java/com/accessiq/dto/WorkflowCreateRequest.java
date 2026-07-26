package com.accessiq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class WorkflowCreateRequest {

    @NotBlank(message = "Workflow name is required")
    @Size(max = 100, message = "Workflow name must not exceed 100 characters")
    private String name;

    @NotEmpty(message = "At least one step is required")
    private List<WorkflowStepRequest> steps;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WorkflowStepRequest> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStepRequest> steps) {
        this.steps = steps;
    }
}