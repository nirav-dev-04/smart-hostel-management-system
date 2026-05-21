package com.hostelmanagement.service.impl;

import com.hostelmanagement.dto.response.AuditLogResponse;
import com.hostelmanagement.entity.AuditLog;
import com.hostelmanagement.repository.AuditLogRepository;
import com.hostelmanagement.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void logAction(String action, String oldValue, String newValue, String performedBy) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .performedBy(performedBy)
                .build();

        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(log -> AuditLogResponse.builder()
                        .id(log.getId())
                        .action(log.getAction())
                        .oldValue(log.getOldValue())
                        .newValue(log.getNewValue())
                        .performedBy(log.getPerformedBy())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}
