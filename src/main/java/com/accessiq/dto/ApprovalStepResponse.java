package com.accessiq.dto;

import com.accessiq.model.ApprovalStatus;

import java.time.LocalDateTime;

public class ApprovalStepResponse {
    private Long id;
    private String approverRole;
    private int stepOrder;
    private ApprovalStatus status;
    private String decidedBy;
    private LocalDateTime decidedAt;
    private LocalDateTime slaDueAt;

    public ApprovalStepResponse() {
    }

    public ApprovalStepResponse(Long id, String approverRole, int stepOrder, ApprovalStatus status,
                                  String decidedBy, LocalDateTime decidedAt, LocalDateTime slaDueAt) {
        this.id = id;
        this.approverRole = approverRole;
        this.stepOrder = stepOrder;
        this.status = status;
        this.decidedBy = decidedBy;
        this.decidedAt = decidedAt;
        this.slaDueAt = slaDueAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApproverRole() {
        return approverRole;
    }

    public void setApproverRole(String approverRole) {
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

    public String getDecidedBy() {
        return decidedBy;
    }

    public void setDecidedBy(String decidedBy) {
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
}