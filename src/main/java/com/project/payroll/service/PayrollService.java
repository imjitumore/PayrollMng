package com.project.payroll.service;

import com.project.payroll.entity.Employee;
import com.project.payroll.entity.Payroll;
import com.project.payroll.repository.EmployeeRepository;
import com.project.payroll.repository.PayrollRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;


@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;

    public PayrollService(PayrollRepository payrollRepository,
                          EmployeeRepository employeeRepository) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Payroll> getAll() {
        return payrollRepository.findAll();
    }

    public Payroll generateSalarySlip(String empId, String month) {

        Optional<Employee> empOpt = employeeRepository.findByEmpId(empId);
        if (empOpt.isEmpty()) {
            return null;
        }

        Payroll payroll = payrollRepository
                .findByEmpIdAndMonth(empId, month)
                .orElse(new Payroll());

        Employee emp = empOpt.get();

        payroll.setEmpId(empId);
        payroll.setMonth(month);

        double basic = emp.getBasicSalary();
        double allowances = basic * 0.20;
        double deductions = basic * 0.10;
        double net = basic + allowances - deductions;

        payroll.setBasicSalary(basic);
        payroll.setAllowances(allowances);
        payroll.setDeductions(deductions);
        payroll.setNetSalary(net);

        return payrollRepository.save(payroll);
    }
}
