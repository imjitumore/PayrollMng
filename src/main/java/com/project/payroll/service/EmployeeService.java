package com.project.payroll.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.project.payroll.entity.Employee;
import com.project.payroll.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
    }

    public Employee save(Employee emp) {
        return repo.save(emp);
    }

    public List<Employee> getAll() {
        return repo.findAll();
    }

    public Optional<Employee> getById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
