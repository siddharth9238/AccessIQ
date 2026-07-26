package com.accessiq.dto;

import java.time.LocalDateTime;
import java.util.List;

public class WorkflowResponse {
    private Long id;
    private String name;
    private boolean active;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<WorkflowStepResponse> steps;

    public WorkflowResponse() {
    }

    public WorkflowResponse(Long id, String name, boolean active, String createdBy,
                            LocalDateTime createdAt, List<WorkflowStepResponse> steps) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.steps = steps;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<WorkflowStepResponse> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStepResponse> steps) {
        this.steps = steps;
    }
}