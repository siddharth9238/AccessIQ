package com.accessiq.repository;

import com.accessiq.model.RoleName;
import com.accessiq.model.WorkflowStepDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkflowStepDefinitionRepository extends JpaRepository<WorkflowStepDefinition, Long> {

    List<WorkflowStepDefinition> findByWorkflowIdOrderByStepOrderAsc(Long workflowId);

    @Query("SELECT w FROM WorkflowStepDefinition w WHERE w.workflow.id = :workflowId AND w.approverRole = :roleName ORDER BY w.stepOrder")
    List<WorkflowStepDefinition> findByWorkflowIdAndApproverRole(@Param("workflowId") Long workflowId, @Param("roleName") RoleName roleName);

    Optional<WorkflowStepDefinition> findByWorkflowIdAndStepOrder(Long workflowId, int stepOrder);

    @Query("SELECT w FROM WorkflowStepDefinition w WHERE w.workflow.id = :workflowId ORDER BY w.stepOrder")
    List<WorkflowStepDefinition> findStepsByWorkflowId(@Param("workflowId") Long workflowId);

    boolean existsByWorkflowIdAndStepOrder(Long workflowId, int stepOrder);
}