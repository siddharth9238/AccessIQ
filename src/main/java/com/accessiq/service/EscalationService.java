package com.accessiq.service;

import com.accessiq.model.ApprovalStatus;
import com.accessiq.model.ApprovalStep;
import com.accessiq.repository.ApprovalStepRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EscalationService {

    private final ApprovalStepRepository approvalStepRepository;
    private final AuditService auditService;

    public EscalationService(ApprovalStepRepository approvalStepRepository, AuditService auditService) {
        this.approvalStepRepository = approvalStepRepository;
        this.auditService = auditService;
    }

    @Scheduled(fixedDelay = 300000)
    public void escalateOverdueSteps() {
        List<ApprovalStep> overdue = approvalStepRepository
                .findByStatusAndSlaDueAtBefore(ApprovalStatus.PENDING, LocalDateTime.now());

        for (ApprovalStep step : overdue) {
            step.setStatus(ApprovalStatus.ESCALATED);
            approvalStepRepository.save(step);
            auditService.log(
                    "SLA_ESCALATED",
                    "system",
                    "Approval step ID: " + step.getId() + " for request ID: " + step.getRequest().getId()
            );
        }
    }
}
