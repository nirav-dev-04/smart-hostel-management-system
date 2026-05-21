package com.hostelmanagement.service;

import com.hostelmanagement.dto.request.ComplaintRequest;
import com.hostelmanagement.dto.response.ComplaintResponse;

import java.util.List;

public interface ComplaintService {
    ComplaintResponse createComplaint(Long studentId, ComplaintRequest request);
    List<ComplaintResponse> getMyComplaints(Long studentId);
    ComplaintResponse getComplaintById(Long complaintId);
    ComplaintResponse updateComplaint(Long complaintId, Long userId, ComplaintRequest request);
    ComplaintResponse uploadComplaintImage(Long complaintId, String imageUrl);
    List<ComplaintResponse> getRectorComplaints(Long rectorId);
    ComplaintResponse updateComplaintStatus(Long complaintId, String statusName);
    ComplaintResponse addResolutionNote(Long complaintId, String resolutionNote);
    ComplaintResponse assignRector(Long complaintId, Long rectorId);
    List<ComplaintResponse> getAllComplaints();
}
