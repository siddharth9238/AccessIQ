package com.accessiq.repository;

import com.accessiq.model.WorkflowStepDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowStepDefinitionRepository extends JpaRepository<WorkflowStepDefinition, Long> {
    List<WorkflowStepDefinition> findByWorkflowIdOrderByStepOrderAsc(Long workflowId);
}
