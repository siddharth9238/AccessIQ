package com.accessiq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class WorkflowCreateRequest {

    @NotBlank
    private String name;

    @NotEmpty
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
