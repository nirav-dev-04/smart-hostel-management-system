package com.hostelmanagement.service;

import com.hostelmanagement.dto.response.AuditLogResponse;

import java.util.List;

public interface AuditLogService {
    void logAction(String action, String oldValue, String newValue, String performedBy);
    List<AuditLogResponse> getAllLogs();
}
