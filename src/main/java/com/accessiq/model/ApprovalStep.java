package com.accessiq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_steps", indexes = {
        @Index(name = "idx_approval_steps_request_id", columnList = "request_id"),
        @Index(name = "idx_approval_steps_status", columnList = "status"),
        @Index(name = "idx_approval_steps_approver_role", columnList = "approver_role"),
        @Index(name = "idx_approval_steps_step_order", columnList = "step_order"),
        @Index(name = "idx_approval_steps_sla_due_at", columnList = "sla_due_at")
})
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_role", nullable = false, length = 20)
    private RoleName approverRole;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "sla_due_at")
    private LocalDateTime slaDueAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
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

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public User getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(User decidedBy) {
        this.decidedBy = decidedBy;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public LocalDateTime getSlaDueAt() {
        return slaDueAt;
    }

    public void setSlaDueAt(LocalDateTime slaDueAt) {
        this.slaDueAt = slaDueAt;
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

    public boolean isOverdue() {
        return slaDueAt != null && LocalDateTime.now().isAfter(slaDueAt) && status == ApprovalStatus.PENDING;
    }
}