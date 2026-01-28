package com.project.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.payroll.entity.Payroll;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByEmpId(String empId);
    Optional<Payroll> findByEmpIdAndMonth(String empId, String month);  // KEY METHOD
}
