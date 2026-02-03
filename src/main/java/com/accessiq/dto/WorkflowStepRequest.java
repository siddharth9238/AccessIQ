package com.accessiq.dto;

import com.accessiq.model.RoleName;
import jakarta.validation.constraints.NotNull;

public class WorkflowStepRequest {

    @NotNull
    private RoleName approverRole;

    @NotNull
    private Integer stepOrder;

    private Integer slaHours;

    public RoleName getApproverRole() {
        return approverRole;
    }

    public void setApproverRole(RoleName approverRole) {
        this.approverRole = approverRole;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public Integer getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }
}
