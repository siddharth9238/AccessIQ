package com.accessiq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "workflow_steps")
public class WorkflowStepDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "workflow_id")
    private WorkflowDefinition workflow;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_role", nullable = false)
    private RoleName approverRole;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "sla_hours")
    private Integer slaHours;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkflowDefinition getWorkflow() {
        return workflow;
    }

    public void setWorkflow(WorkflowDefinition workflow) {
        this.workflow = workflow;
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
