package com.accessiq.repository;

import com.accessiq.model.WorkflowDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {

    Optional<WorkflowDefinition> findByName(String name);

    Optional<WorkflowDefinition> findFirstByActiveTrueOrderByIdAsc();

    List<WorkflowDefinition> findByActiveTrueOrderByIdAsc();

    Page<WorkflowDefinition> findByActiveTrue(Pageable pageable);

    Page<WorkflowDefinition> findAll(Pageable pageable);

    @Query("SELECT w FROM WorkflowDefinition w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<WorkflowDefinition> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT w FROM WorkflowDefinition w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<WorkflowDefinition> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByName(String name);
}