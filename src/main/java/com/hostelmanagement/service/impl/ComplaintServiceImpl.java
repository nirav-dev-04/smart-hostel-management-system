package com.hostelmanagement.service.impl;

import com.hostelmanagement.dto.request.ComplaintRequest;
import com.hostelmanagement.dto.response.ComplaintResponse;
import com.hostelmanagement.entity.Complaint;
import com.hostelmanagement.entity.User;
import com.hostelmanagement.enums.ComplaintCategory;
import com.hostelmanagement.enums.ComplaintPriority;
import com.hostelmanagement.enums.ComplaintStatus;
import com.hostelmanagement.enums.Role;
import com.hostelmanagement.exception.ResourceNotFoundException;
import com.hostelmanagement.exception.UnauthorizedException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.mapper.ComplaintMapper;
import com.hostelmanagement.repository.ComplaintRepository;
import com.hostelmanagement.repository.UserRepository;
import com.hostelmanagement.service.ComplaintService;
import com.hostelmanagement.service.NotificationService;
import com.hostelmanagement.service.AuditLogService;
import com.hostelmanagement.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                                UserRepository userRepository,
                                NotificationService notificationService,
                                AuditLogService auditLogService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public ComplaintResponse createComplaint(Long studentId, ComplaintRequest request) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new ValidationException("Only students can file complaints!");
        }

        ComplaintCategory category;
        try {
            category = ComplaintCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid complaint category: " + request.getCategory());
        }

        ComplaintPriority priority = ComplaintPriority.MEDIUM;
        if (request.getPriority() != null) {
            try {
                priority = ComplaintPriority.valueOf(request.getPriority().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid priority value: " + request.getPriority());
            }
        } else if (category == ComplaintCategory.EMERGENCY) {
            priority = ComplaintPriority.EMERGENCY;
        }

        Complaint complaint = Complaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(category)
                .priority(priority)
                .status(ComplaintStatus.PENDING)
                .student(student)
                .hostelBlock(student.getHostelBlock())
                .build();

        // Auto-assign rector of same hostel block if available
        List<User> blockRectors = userRepository.findByRole(Role.RECTOR).stream()
                .filter(r -> r.getHostelBlock() != null && r.getHostelBlock().equalsIgnoreCase(student.getHostelBlock()))
                .toList();

        if (!blockRectors.isEmpty()) {
            complaint.setRector(blockRectors.get(0)); // Assign first matching rector
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Notify Student
        notificationService.sendNotification(student, 
                String.format("Your complaint #%d '%s' has been submitted successfully.", savedComplaint.getId(), savedComplaint.getTitle()));

        // Notify assigned Rector
        if (savedComplaint.getRector() != null) {
            notificationService.sendNotification(savedComplaint.getRector(), 
                    String.format("A new complaint #%d in block %s has been auto-assigned to you.", savedComplaint.getId(), savedComplaint.getHostelBlock()));
        }

        String performer = SecurityUtils.getCurrentUserEmail();
        auditLogService.logAction("CREATE_COMPLAINT", null, savedComplaint.getTitle(), performer);

        return ComplaintMapper.toResponse(savedComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getMyComplaints(Long studentId) {
        return complaintRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(ComplaintMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));
        return ComplaintMapper.toResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaint(Long complaintId, Long userId, ComplaintRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        // Enforce owner / Admin check
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!complaint.getStudent().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You are not authorized to update this complaint!");
        }

        // Only allowed to update when complaint is PENDING
        if (complaint.getStatus() != ComplaintStatus.PENDING) {
            throw new ValidationException("Only complaints in PENDING status can be edited!");
        }

        String oldValue = String.format("Title: %s, Desc: %s", complaint.getTitle(), complaint.getDescription());

        complaint.setTitle(request.getTitle());
        complaint.setDescription(request.getDescription());

        try {
            complaint.setCategory(ComplaintCategory.valueOf(request.getCategory().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid complaint category: " + request.getCategory());
        }

        if (request.getPriority() != null) {
            try {
                complaint.setPriority(ComplaintPriority.valueOf(request.getPriority().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid priority value: " + request.getPriority());
            }
        }

        Complaint updatedComplaint = complaintRepository.save(complaint);
        String newValue = String.format("Title: %s, Desc: %s", updatedComplaint.getTitle(), updatedComplaint.getDescription());

        String performer = SecurityUtils.getCurrentUserEmail();
        auditLogService.logAction("UPDATE_COMPLAINT", oldValue, newValue, performer);

        return ComplaintMapper.toResponse(updatedComplaint);
    }

    @Override
    @Transactional
    public ComplaintResponse uploadComplaintImage(Long complaintId, String imageUrl) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        complaint.setImageUrl(imageUrl);
        Complaint updatedComplaint = complaintRepository.save(complaint);

        String performer = SecurityUtils.getCurrentUserEmail();
        auditLogService.logAction("COMPLAINT_IMAGE_UPLOAD", null, imageUrl, performer);

        return ComplaintMapper.toResponse(updatedComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getRectorComplaints(Long rectorId) {
        User rector = userRepository.findById(rectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Rector not found with ID: " + rectorId));

        if (rector.getRole() != Role.RECTOR) {
            throw new ValidationException("Only rector accounts can fetch assigned complaints!");
        }

        // Return complaints in their block
        return complaintRepository.findByHostelBlockOrderByCreatedAtDesc(rector.getHostelBlock()).stream()
                .map(ComplaintMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComplaintResponse updateComplaintStatus(Long complaintId, String statusName) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        ComplaintStatus oldStatus = complaint.getStatus();
        ComplaintStatus newStatus;
        try {
            newStatus = ComplaintStatus.valueOf(statusName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid complaint status: " + statusName);
        }

        complaint.setStatus(newStatus);
        Complaint updatedComplaint = complaintRepository.save(complaint);

        // Notify Student
        notificationService.sendNotification(complaint.getStudent(), 
                String.format("The status of your complaint #%d '%s' has been updated to %s.", 
                        complaint.getId(), complaint.getTitle(), newStatus.name()));

        String performer = SecurityUtils.getCurrentUserEmail();
        auditLogService.logAction("COMPLAINT_STATUS_UPDATE", oldStatus.name(), newStatus.name(), performer);

        return ComplaintMapper.toResponse(updatedComplaint);
    }

    @Override
    @Transactional
    public ComplaintResponse addResolutionNote(Long complaintId, String resolutionNote) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        String oldNote = complaint.getResolutionNote();
        complaint.setResolutionNote(resolutionNote);
        
        // Auto-resolve when resolution note is added by default
        complaint.setStatus(ComplaintStatus.RESOLVED);
        
        Complaint updatedComplaint = complaintRepository.save(complaint);

        // Notify Student
        notificationService.sendNotification(complaint.getStudent(), 
                String.format("Your complaint #%d has been resolved. Note: %s", complaint.getId(), resolutionNote));

        String performer = SecurityUtils.getCurrentUserEmail();
        auditLogService.logAction("COMPLAINT_RESOLUTION_NOTE", oldNote, resolutionNote, performer);

        return ComplaintMapper.toResponse(updatedComplaint);
    }

    @Override
    @Transactional
    public ComplaintResponse assignRector(Long complaintId, Long rectorId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        User rector = userRepository.findById(rectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Rector not found with ID: " + rectorId));

        if (rector.getRole() != Role.RECTOR) {
            throw new ValidationException("User is not a Rector!");
        }

        String oldRectorName = complaint.getRector() != null ? complaint.getRector().getEmail() : "Unassigned";
        complaint.setRector(rector);
        
        // When rector is assigned manually, we update status to IN_PROGRESS
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);

        Complaint updatedComplaint = complaintRepository.save(complaint);

        // Notify Student
        notificationService.sendNotification(complaint.getStudent(), 
                String.format("Rector %s has been assigned to resolve your complaint #%d.", rector.getName(), complaint.getId()));

        // Notify Rector
        notificationService.sendNotification(rector, 
                String.format("You have been assigned to resolve complaint #%d '%s' in Block %s.", 
                        complaint.getId(), complaint.getTitle(), complaint.getHostelBlock()));

        String performer = SecurityUtils.getCurrentUserEmail();
        auditLogService.logAction("COMPLAINT_REASSIGN_RECTOR", oldRectorName, rector.getEmail(), performer);

        return ComplaintMapper.toResponse(updatedComplaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAll().stream()
                .map(ComplaintMapper::toResponse)
                .collect(Collectors.toList());
    }
}
