package com.accessiq.service;

import com.accessiq.model.ApprovalStatus;
import com.accessiq.model.ApprovalStep;
import com.accessiq.repository.ApprovalStepRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for escalation of overdue approval steps.
 */
@Service
public class EscalationService {

    /** Logger for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(EscalationService.class);

    /** The approval step repository. */
    private final ApprovalStepRepository approvalStepRepository;

    /** The audit service. */
    private final AuditService auditService;

    /**
     * Constructs a new EscalationService.
     *
     * @param approvalStepRepository the approval step repository
     * @param auditService the audit service
     */
    public EscalationService(final ApprovalStepRepository approvalStepRepository,
            final AuditService auditService) {
        this.approvalStepRepository = approvalStepRepository;
        this.auditService = auditService;
    }

    /**
     * Escalates overdue approval steps.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void escalateOverdueSteps() {
        LOG.info("Checking for overdue approval steps");

        final List<ApprovalStep> overdue = approvalStepRepository
                .findByStatusAndSlaDueAtBefore(
                        ApprovalStatus.PENDING, LocalDateTime.now());

        int escalatedCount = 0;
        for (final ApprovalStep step : overdue) {
            step.setStatus(ApprovalStatus.ESCALATED);
            approvalStepRepository.save(step);
            auditService.log("SLA_ESCALATED", "system",
                    "Approval step ID: " + step.getId()
                            + " for request ID: " + step.getRequest().getId());
            escalatedCount++;
        }

        if (escalatedCount > 0) {
            LOG.info("Escalated {} overdue approval steps", escalatedCount);
        } else {
            LOG.debug("No overdue approval steps found");
        }
    }
}