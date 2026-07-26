package com.accessiq.service;

import com.accessiq.dto.AuditLogResponse;
import com.accessiq.model.AuditLog;
import com.accessiq.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for audit logging operations.
 */
@Service
@Transactional
public class AuditService {

    /** Logger for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

    /** The audit log repository. */
    private final AuditLogRepository auditLogRepository;

    /**
     * Constructs a new AuditService.
     *
     * @param auditLogRepository the audit log repository
     */
    public AuditService(final AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Logs an audit event.
     *
     * @param action the action performed
     * @param performedBy the user who performed the action
     * @param details the details of the action
     */
    public void log(final String action, final String performedBy, final String details) {
        final AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setPerformedBy(performedBy);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
        LOG.debug("Audit log created: {} by {}", action, performedBy);
    }

    /**
     * Lists all audit logs.
     *
     * @return the list of audit logs
     */
    @Transactional(readOnly = true)
    public List<AuditLog> listLogs() {
        return auditLogRepository.findAll();
    }

    /**
     * Lists all audit logs with pagination.
     *
     * @param pageable the pageable
     * @return the page of audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> listLogs(final Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Lists audit logs by performer.
     *
     * @param performedBy the performer
     * @return the list of audit logs
     */
    @Transactional(readOnly = true)
    public List<AuditLog> listLogsByPerformedBy(final String performedBy) {
        return auditLogRepository.findByPerformedBy(performedBy);
    }

    /**
     * Lists audit logs by action.
     *
     * @param action the action
     * @return the list of audit logs
     */
    @Transactional(readOnly = true)
    public List<AuditLog> listLogsByAction(final String action) {
        return auditLogRepository.findByAction(action);
    }

    /**
     * Gets audit log responses with optional filtering.
     *
     * @param action the action filter
     * @param performedBy the performer filter
     * @return the list of audit log responses
     */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogResponses(final String action,
            final String performedBy) {
        final List<AuditLog> logs;
        if (action != null && performedBy != null) {
            logs = auditLogRepository.findByActionAndPerformedBy(action, performedBy);
        } else if (action != null) {
            logs = auditLogRepository.findByAction(action);
        } else if (performedBy != null) {
            logs = auditLogRepository.findByPerformedBy(performedBy);
        } else {
            logs = auditLogRepository.findTop100ByOrderByTimestampDesc();
        }

        return logs.stream()
                .map(this::toAuditLogResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets audit log responses with pagination.
     *
     * @param pageable the pageable
     * @return the page of audit log responses
     */
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogResponses(final Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(this::toAuditLogResponse);
    }

    /**
     * Converts an audit log to a response.
     *
     * @param log the audit log
     * @return the audit log response
     */
    public AuditLogResponse toAuditLogResponse(final AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getPerformedBy(),
                log.getTimestamp(),
                log.getDetails()
        );
    }
}