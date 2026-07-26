package com.accessiq.repository;

import com.accessiq.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByPerformedBy(String performedBy);

    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);

    List<AuditLog> findByAction(String action);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.action = :action AND a.performedBy = :performedBy")
    List<AuditLog> findByActionAndPerformedBy(@Param("action") String action, @Param("performedBy") String performedBy);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end")
    List<AuditLog> findByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end")
    Page<AuditLog> findByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    List<AuditLog> findTop100ByOrderByTimestampDesc();

    @Query("SELECT a FROM AuditLog a WHERE a.performedBy = :performedBy AND a.action = :action")
    List<AuditLog> findByPerformedByAndAction(@Param("performedBy") String performedBy, @Param("action") String action);

    @Query("SELECT a FROM AuditLog a WHERE a.performedBy = :performedBy AND a.action = :action")
    Page<AuditLog> findByPerformedByAndAction(@Param("performedBy") String performedBy,
            @Param("action") String action, Pageable pageable);
}