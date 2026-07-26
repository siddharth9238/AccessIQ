package com.accessiq.repository;

import com.accessiq.model.ApprovalStatus;
import com.accessiq.model.ApprovalStep;
import com.accessiq.model.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {

    Optional<ApprovalStep> findFirstByRequestIdAndStatusOrderByStepOrderAsc(Long requestId, ApprovalStatus status);

    Optional<ApprovalStep> findFirstByRequestIdAndStatusAndApproverRole(Long requestId, ApprovalStatus status, RoleName roleName);

    List<ApprovalStep> findByRequestIdOrderByStepOrderAsc(Long requestId);

    Page<ApprovalStep> findByRequestId(Long requestId, Pageable pageable);

    List<ApprovalStep> findByStatusAndSlaDueAtBefore(ApprovalStatus status, LocalDateTime now);

    @Query("SELECT a FROM ApprovalStep a WHERE a.request.id = :requestId AND a.status = :status")
    List<ApprovalStep> findByRequestIdAndStatus(@Param("requestId") Long requestId, @Param("status") ApprovalStatus status);

    @Query("SELECT a FROM ApprovalStep a WHERE a.decidedBy.email = :email")
    List<ApprovalStep> findByDecidedByEmail(@Param("email") String email);

    @Query("SELECT a FROM ApprovalStep a WHERE a.slaDueAt < :now AND a.status = :status")
    List<ApprovalStep> findOverdueByStatus(@Param("now") LocalDateTime now, @Param("status") ApprovalStatus status);

    long countByStatus(ApprovalStatus status);
}