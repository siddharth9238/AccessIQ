package com.accessiq.controller;

import com.accessiq.dto.ApiResponse;
import com.accessiq.dto.AuditLogResponse;
import com.accessiq.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> listLogs(
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        logger.info("Listing audit logs - performedBy: {}, action: {}", performedBy, action);
        
        List<AuditLogResponse> logs;
        if (performedBy != null && action != null) {
            logs = auditLogRepository.findByActionAndPerformedBy(action, performedBy)
                    .stream()
                    .map(this::toAuditLogResponse)
                    .toList();
        } else if (performedBy != null) {
            logs = auditLogRepository.findByPerformedBy(performedBy)
                    .stream()
                    .map(this::toAuditLogResponse)
                    .toList();
        } else if (action != null) {
            logs = auditLogRepository.findByAction(action)
                    .stream()
                    .map(this::toAuditLogResponse)
                    .toList();
        } else {
            logs = auditLogRepository.findTop100ByOrderByTimestampDesc()
                    .stream()
                    .map(this::toAuditLogResponse)
                    .toList();
        }
        
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/logs/page")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> listLogsPaged(
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 50) Pageable pageable
    ) {
        logger.info("Listing paged audit logs - performedBy: {}, action: {}", performedBy, action);
        
        Page<AuditLogResponse> logs;
        if (performedBy != null && action != null) {
            logs = auditLogRepository.findByPerformedByAndAction(performedBy, action, pageable)
                    .map(this::toAuditLogResponse);
        } else if (performedBy != null) {
            logs = auditLogRepository.findByPerformedBy(performedBy, pageable)
                    .map(this::toAuditLogResponse);
        } else if (action != null) {
            logs = auditLogRepository.findByAction(action, pageable)
                    .map(this::toAuditLogResponse);
        } else {
            logs = auditLogRepository.findAll(pageable)
                    .map(this::toAuditLogResponse);
        }
        
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/logs/search")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> searchLogs(
            @RequestParam String keyword
    ) {
        logger.info("Searching audit logs with keyword: {}", keyword);
        
        List<AuditLogResponse> logs = auditLogRepository.findAll()
                .stream()
                .filter(log -> log.getAction().toLowerCase().contains(keyword.toLowerCase())
                        || log.getPerformedBy().toLowerCase().contains(keyword.toLowerCase())
                        || (log.getDetails() != null && log.getDetails().toLowerCase().contains(keyword.toLowerCase())))
                .map(this::toAuditLogResponse)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    private AuditLogResponse toAuditLogResponse(com.accessiq.model.AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getPerformedBy(),
                log.getTimestamp(),
                log.getDetails()
        );
    }
}