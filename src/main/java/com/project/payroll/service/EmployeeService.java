package com.project.payroll.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.project.payroll.entity.Employee;
import com.project.payroll.entity.User;
import com.project.payroll.repository.EmployeeRepository;
import com.project.payroll.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class EmployeeService {

    private final EmployeeRepository repo;

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Value("${app.default-password:company123}")
    private String defaultPassword;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
    }

    public Employee save(Employee emp) {
        Employee persisted = repo.save(emp);
        // business rule: automatically create login for new employee
        if (persisted.getEmpId() != null) {
            String username = persisted.getEmpId() + "@jum.com";
            if (userService.findByUsername(username) == null) {
                User user = new User();
                user.setUsername(username);
                user.setEmpId(persisted.getEmpId());
                user.setPassword(encoder.encode(defaultPassword));
                user.setPasswordChanged(false);
                userService.updateProfile(user);
            }
        }
        return persisted;
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
