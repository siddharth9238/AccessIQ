package com.accessiq.dto;

import com.accessiq.model.RoleName;
import jakarta.validation.constraints.NotNull;

public class WorkflowStepRequest {

    @NotNull(message = "Approver role is required")
    private RoleName approverRole;

    @NotNull(message = "Step order is required")
    private Integer stepOrder;

    private Integer slaHours;

    /**
     * Default constructor.
     */
    public WorkflowStepRequest() {
    }

    /**
     * Constructs a new WorkflowStepRequest.
     *
     * @param approverRole the approver role
     * @param stepOrder the step order
     * @param slaHours the SLA hours
     */
    public WorkflowStepRequest(final RoleName approverRole, final int stepOrder,
            final int slaHours) {
        this.approverRole = approverRole;
        this.stepOrder = stepOrder;
        this.slaHours = slaHours;
    }

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