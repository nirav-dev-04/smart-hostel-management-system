package com.hostelmanagement.scheduler;

import com.hostelmanagement.entity.Complaint;
import com.hostelmanagement.entity.User;
import com.hostelmanagement.enums.ComplaintStatus;
import com.hostelmanagement.enums.Role;
import com.hostelmanagement.repository.ComplaintRepository;
import com.hostelmanagement.repository.UserRepository;
import com.hostelmanagement.service.NotificationService;
import com.hostelmanagement.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class EscalationScheduler {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public EscalationScheduler(ComplaintRepository complaintRepository,
                               UserRepository userRepository,
                               NotificationService notificationService,
                               AuditLogService auditLogService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    /**
     * Runs every hour (3600000 ms) with an initial delay of 10 seconds.
     * Automatically escalates complaints that are in PENDING or IN_PROGRESS state and older than 48 hours.
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 10000)
    @Transactional
    public void autoEscalateComplaints() {
        log.info("Running automatic complaint escalation scheduler...");

        LocalDateTime thresholdTime = LocalDateTime.now().minusHours(48);
        List<ComplaintStatus> openStatuses = List.of(ComplaintStatus.PENDING, ComplaintStatus.IN_PROGRESS);

        List<Complaint> complaintsToEscalate = complaintRepository
                .findByStatusInAndCreatedAtBefore(openStatuses, thresholdTime);

        if (complaintsToEscalate.isEmpty()) {
            log.info("No complaints found qualifying for auto-escalation.");
            return;
        }

        log.info("Found {} complaints qualifying for auto-escalation. Escalating...", complaintsToEscalate.size());

        // Get all system administrators to notify them
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        for (Complaint complaint : complaintsToEscalate) {
            String oldStatus = complaint.getStatus().name();
            complaint.setStatus(ComplaintStatus.ESCALATED);
            complaintRepository.save(complaint);

            // Log inside audit trail
            auditLogService.logAction("AUTO_ESCALATION", oldStatus, ComplaintStatus.ESCALATED.name(), "system_scheduler");

            // Notify Student
            notificationService.sendNotification(complaint.getStudent(),
                    String.format("Your complaint #%d '%s' has been auto-escalated to ADMINS due to no resolution in 48 hours.", 
                            complaint.getId(), complaint.getTitle()));

            // Notify assigned Rector
            if (complaint.getRector() != null) {
                notificationService.sendNotification(complaint.getRector(),
                        String.format("Complaint #%d assigned to you has been auto-escalated to administrators.", complaint.getId()));
            }

            // Notify all system Administrators
            for (User admin : admins) {
                notificationService.sendNotification(admin,
                        String.format("URGENT: Complaint #%d in block %s has been auto-escalated. Action required.", 
                                complaint.getId(), complaint.getHostelBlock()));
            }

            log.info("Complaint ID {} successfully escalated.", complaint.getId());
        }
    }
}
