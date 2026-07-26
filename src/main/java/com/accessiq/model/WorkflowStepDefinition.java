package com.accessiq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_steps", indexes = {
        @Index(name = "idx_workflow_steps_workflow_id", columnList = "workflow_id"),
        @Index(name = "idx_workflow_steps_approver_role", columnList = "approver_role"),
        @Index(name = "idx_workflow_steps_step_order", columnList = "step_order")
})
public class WorkflowStepDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    private WorkflowDefinition workflow;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_role", nullable = false, length = 20)
    private RoleName approverRole;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}