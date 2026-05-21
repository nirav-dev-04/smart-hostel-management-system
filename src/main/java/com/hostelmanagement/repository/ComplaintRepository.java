package com.hostelmanagement.repository;

import com.hostelmanagement.entity.Complaint;
import com.hostelmanagement.enums.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Complaint> findByRectorIdOrderByCreatedAtDesc(Long rectorId);

    List<Complaint> findByHostelBlockOrderByCreatedAtDesc(String hostelBlock);

    List<Complaint> findByStatusInAndCreatedAtBefore(List<ComplaintStatus> statuses, LocalDateTime dateTime);

    List<Complaint> findByStatusIn(List<ComplaintStatus> statuses);

    @Query("SELECT COUNT(c) FROM Complaint c")
    long countAllComplaints();

    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status")
    long countByStatus(ComplaintStatus status);

    @Query("SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category")
    List<Object[]> countComplaintsByCategory();

    @Query("SELECT c.hostelBlock, COUNT(c) FROM Complaint c GROUP BY c.hostelBlock")
    List<Object[]> countComplaintsByHostelBlock();

    @Query("SELECT c.rector.name, COUNT(c) FROM Complaint c WHERE c.status = 'RESOLVED' OR c.status = 'CLOSED' GROUP BY c.rector.name")
    List<Object[]> countResolvedComplaintsByRector();
}
