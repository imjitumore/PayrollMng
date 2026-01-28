package com.project.payroll.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payroll")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empId;
    private String month;
    private Double basicSalary;
    private Double allowances;
    private Double deductions;
    private Double netSalary;

    // ===== GETTERS & SETTERS =====
    public Long getId() {
        return id;
    }

    public String getEmpId() {
        return empId;
    }
    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getMonth() {
        return month;
    }
    public void setMonth(String month) {
        this.month = month;
    }

    public Double getBasicSalary() {
        return basicSalary;
    }
    public void setBasicSalary(Double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public Double getAllowances() {
        return allowances;
    }
    public void setAllowances(Double allowances) {
        this.allowances = allowances;
    }

    public Double getDeductions() {
        return deductions;
    }
    public void setDeductions(Double deductions) {
        this.deductions = deductions;
    }

    public Double getNetSalary() {
        return netSalary;
    }
    public void setNetSalary(Double netSalary) {
        this.netSalary = netSalary;
    }
}
