package com.project.payroll.repository;

import com.project.payroll.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmpId(String empId);

    // helper to find requests that include a specific day
    List<LeaveRequest> findByEmpIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            String empId, LocalDate from, LocalDate to);
}
