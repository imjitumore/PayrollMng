package com.project.payroll.service;

import com.project.payroll.entity.LeaveRequest;
import com.project.payroll.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository repository;

    public LeaveRequestService(LeaveRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LeaveRequest save(LeaveRequest req) {
        if (req.getStatus() == null) {
            req.setStatus("Pending");
        }
        return repository.save(req);
    }

    public List<LeaveRequest> getByEmpId(String empId) {
        return repository.findByEmpId(empId);
    }

    public List<LeaveRequest> getAll() {
        return repository.findAll();
    }

    public void updateStatus(Long id, String status) {
        repository.findById(id).ifPresent(r -> {
            r.setStatus(status);
            repository.save(r);
        });
    }

    public List<LeaveRequest> findOverlapping(String empId, LocalDate date) {
        return repository.findByEmpIdAndFromDateLessThanEqualAndToDateGreaterThanEqual(empId, date, date);
    }
}
