package com.project.payroll.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "attendance",
    uniqueConstraints = @UniqueConstraint(columnNames = {"empId", "date"})
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empId;
    private String employeeName;
    private LocalDate date;
    private String status; // Present / Absent / Leave

    // Getters & Setters
    public Long getId() { return id; }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
