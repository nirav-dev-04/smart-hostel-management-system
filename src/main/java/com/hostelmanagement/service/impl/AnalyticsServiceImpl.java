package com.hostelmanagement.service.impl;

import com.hostelmanagement.dto.response.DashboardAnalyticsResponse;
import com.hostelmanagement.entity.Complaint;
import com.hostelmanagement.enums.ComplaintStatus;
import com.hostelmanagement.repository.ComplaintRepository;
import com.hostelmanagement.service.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ComplaintRepository complaintRepository;

    public AnalyticsServiceImpl(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getDashboardAnalytics() {
        long total = complaintRepository.countAllComplaints();
        long pending = complaintRepository.countByStatus(ComplaintStatus.PENDING);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long closed = complaintRepository.countByStatus(ComplaintStatus.CLOSED);

        // Map category stats
        Map<String, Long> categoryStats = new HashMap<>();
        List<Object[]> categoriesRaw = complaintRepository.countComplaintsByCategory();
        for (Object[] row : categoriesRaw) {
            if (row[0] != null) {
                categoryStats.put(row[0].toString(), (Long) row[1]);
            }
        }

        // Map block stats
        Map<String, Long> blockStats = new HashMap<>();
        List<Object[]> blocksRaw = complaintRepository.countComplaintsByHostelBlock();
        for (Object[] row : blocksRaw) {
            if (row[0] != null) {
                blockStats.put(row[0].toString(), (Long) row[1]);
            }
        }

        // Map rector performance stats
        Map<String, Long> rectorPerformance = new HashMap<>();
        List<Object[]> rectorsRaw = complaintRepository.countResolvedComplaintsByRector();
        for (Object[] row : rectorsRaw) {
            if (row[0] != null) {
                rectorPerformance.put(row[0].toString(), (Long) row[1]);
            }
        }

        // Calculate average resolution time programmatically to ensure db independence
        List<Complaint> resolvedComplaints = complaintRepository.findByStatusIn(List.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED));
        double avgResolutionTime = 0.0;
        if (!resolvedComplaints.isEmpty()) {
            double totalHours = 0.0;
            for (Complaint c : resolvedComplaints) {
                if (c.getUpdatedAt() != null && c.getCreatedAt() != null) {
                    Duration duration = Duration.between(c.getCreatedAt(), c.getUpdatedAt());
                    totalHours += duration.toMinutes() / 60.0;
                }
            }
            avgResolutionTime = totalHours / resolvedComplaints.size();
        }

        return DashboardAnalyticsResponse.builder()
                .totalComplaints(total)
                .pendingComplaints(pending)
                .resolvedComplaints(resolved + closed)
                .categoryStats(categoryStats)
                .blockStats(blockStats)
                .rectorPerformance(rectorPerformance)
                .avgResolutionTime(Math.round(avgResolutionTime * 100.0) / 100.0) // Round to 2 decimal places
                .build();
    }
}
