package com.accessiq.repository;

import com.accessiq.model.ApprovalStatus;
import com.accessiq.model.ApprovalStep;
import com.accessiq.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    Optional<ApprovalStep> findFirstByRequestIdAndStatusOrderByStepOrderAsc(Long requestId, ApprovalStatus status);
    Optional<ApprovalStep> findFirstByRequestIdAndStatusAndApproverRole(Long requestId, ApprovalStatus status, RoleName roleName);
    List<ApprovalStep> findByRequestIdOrderByStepOrderAsc(Long requestId);
    List<ApprovalStep> findByStatusAndSlaDueAtBefore(ApprovalStatus status, LocalDateTime now);
}
