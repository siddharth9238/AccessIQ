package com.accessiq.controller;

import com.accessiq.model.AuditLog;
import com.accessiq.repository.AuditLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/logs")
    public List<AuditLog> listLogs(@RequestParam(required = false) String performedBy) {
        if (performedBy != null && !performedBy.isBlank()) {
            return auditLogRepository.findByPerformedBy(performedBy);
        }
        return auditLogRepository.findAll();
    }
}
