package com.accessiq.dto;

import jakarta.validation.constraints.Size;

public class ApprovalDecisionRequest {

    @Size(max = 2000, message = "Details must not exceed 2000 characters")
    private String details;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}