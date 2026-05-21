package com.hostelmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAnalyticsResponse {
    private Long totalComplaints;
    private Long pendingComplaints;
    private Long resolvedComplaints;
    private Map<String, Long> categoryStats;
    private Map<String, Long> blockStats;
    private Map<String, Long> rectorPerformance;
    private Double avgResolutionTime; // In hours
}
