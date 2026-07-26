package com.accessiq.dto;

import com.accessiq.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;

public class RequestResponse {
    private Long id;
    private String title;
    private String description;
    private RequestStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<ApprovalStepResponse> approvalSteps;

    public RequestResponse() {
    }

    public RequestResponse(Long id, String title, String description, RequestStatus status,
                           String createdBy, LocalDateTime createdAt, List<ApprovalStepResponse> approvalSteps) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.approvalSteps = approvalSteps;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
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

    public List<ApprovalStepResponse> getApprovalSteps() {
        return approvalSteps;
    }

    public void setApprovalSteps(List<ApprovalStepResponse> approvalSteps) {
        this.approvalSteps = approvalSteps;
    }
}