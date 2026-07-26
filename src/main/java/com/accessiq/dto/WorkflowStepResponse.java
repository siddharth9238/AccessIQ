package com.accessiq.dto;

import com.accessiq.model.RoleName;

public class WorkflowStepResponse {
    private Long id;
    private RoleName approverRole;
    private int stepOrder;
    private Integer slaHours;

    public WorkflowStepResponse() {
    }

    public WorkflowStepResponse(Long id, RoleName approverRole, int stepOrder, Integer slaHours) {
        this.id = id;
        this.approverRole = approverRole;
        this.stepOrder = stepOrder;
        this.slaHours = slaHours;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleName getApproverRole() {
        return approverRole;
    }

    public void setApproverRole(RoleName approverRole) {
        this.approverRole = approverRole;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public Integer getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }
}