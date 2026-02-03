package com.accessiq.repository;

import com.accessiq.model.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    Optional<WorkflowDefinition> findByName(String name);
    Optional<WorkflowDefinition> findFirstByActiveTrueOrderByIdAsc();
}
