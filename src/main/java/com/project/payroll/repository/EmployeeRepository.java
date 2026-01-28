package com.project.payroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.payroll.entity.Employee;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmpId(String empId);  // Returns Optional<Employee>
}
