package com.project.payroll;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PayrollApplicationTests {

    @Autowired
    private com.project.payroll.service.LeaveRequestService leaveService;

    @Autowired
    private com.project.payroll.repository.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder;

    @Autowired
    private com.project.payroll.service.EmployeeService employeeService;

    @Test
    void contextLoads() {
    }

    @Test
    void leaveRequestLifecycle() {
        com.project.payroll.entity.LeaveRequest req = new com.project.payroll.entity.LeaveRequest();
        req.setEmpId("E001");
        req.setFromDate(java.time.LocalDate.now());
        req.setToDate(java.time.LocalDate.now());
        req.setLeaveType("Casual");
        req.setDuration("Full");
        req.setReason("Testing");

        com.project.payroll.entity.LeaveRequest saved = leaveService.save(req);
        org.junit.jupiter.api.Assertions.assertNotNull(saved.getId());
        org.junit.jupiter.api.Assertions.assertEquals("Pending", saved.getStatus());

        leaveService.updateStatus(saved.getId(), "Approved");
        com.project.payroll.entity.LeaveRequest fetched = leaveService.getByEmpId("E001").get(0);
        org.junit.jupiter.api.Assertions.assertEquals("Approved", fetched.getStatus());
    }

    @Test
    void passwordHashing() {
        com.project.payroll.entity.User user = new com.project.payroll.entity.User();
        user.setUsername("testUser");
        user.setPassword(encoder.encode("secret"));
        userRepository.save(user);

        com.project.payroll.entity.User retrieved = userRepository.findByUsername("testUser").orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches("secret", retrieved.getPassword()));
        org.junit.jupiter.api.Assertions.assertFalse("secret".equals(retrieved.getPassword()));
    }

    @Test
    void employeeUsernameGenerated() {
        com.project.payroll.entity.Employee e = new com.project.payroll.entity.Employee();
        e.setEmpId("2853458");
        e.setFullName("Jane Doe");
        e.setDepartment("IT");
        e.setDesignation("Developer");
        e.setBasicSalary(50000.0);

        com.project.payroll.entity.Employee saved = employeeService.save(e);
        String expectedUsername = "2853458@jum.com";
        com.project.payroll.entity.User u = userRepository.findByUsername(expectedUsername).orElse(null);
        org.junit.jupiter.api.Assertions.assertNotNull(u, "user should be created with @jum.com domain");
        org.junit.jupiter.api.Assertions.assertEquals(saved.getEmpId(), u.getEmpId());
    }

}
