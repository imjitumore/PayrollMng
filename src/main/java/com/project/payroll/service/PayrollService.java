package com.project.payroll.service;

import com.project.payroll.entity.Employee;
import com.project.payroll.entity.Payroll;
import com.project.payroll.entity.Attendance;
import com.project.payroll.repository.EmployeeRepository;
import com.project.payroll.repository.PayrollRepository;
import com.project.payroll.repository.AttendanceRepository;

import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.Optional;
import java.util.List;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    public PayrollService(PayrollRepository payrollRepository,
                          EmployeeRepository employeeRepository,
                          AttendanceRepository attendanceRepository) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public List<Payroll> getAll() {
        return payrollRepository.findAll();
    }

    // ================== UPDATED METHOD ==================

    public Payroll generateSalarySlip(String empId, String month) {

        Optional<Employee> empOpt = employeeRepository.findByEmpId(empId);

        if (empOpt.isEmpty()) {
            return null;
        }

        Employee emp = empOpt.get();

        // Convert month (example: 2025-02)
        YearMonth yearMonth = YearMonth.parse(month);

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        // 1️⃣ Get attendance records for month
        List<Attendance> attendanceList =
                attendanceRepository.findByEmpIdAndDateBetween(empId, start, end);

        // 2️⃣ Count Present days
        long presentDays = attendanceList.stream()
                .filter(a -> "Present".equalsIgnoreCase(a.getStatus()))
                .count();

        // 3️⃣ Calculate total working days (exclude Sundays)
        long workingDays = 0;

        for (int i = 1; i <= yearMonth.lengthOfMonth(); i++) {
            LocalDate date = yearMonth.atDay(i);
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
        }

        // 4️⃣ Calculate salary per day
        double basic = emp.getBasicSalary();
        double perDaySalary = basic / workingDays;

        // 5️⃣ Calculate absent days
        long absentDays = workingDays - presentDays;

        // 6️⃣ Attendance based deduction
        double attendanceDeduction = absentDays * perDaySalary;

        // 7️⃣ Other salary components
        double allowances = basic * 0.20;      // 20% allowance
        double otherDeductions = basic * 0.10; // 10% fixed deduction

        double totalDeductions = otherDeductions + attendanceDeduction;

        double netSalary = basic + allowances - totalDeductions;

        // 8️⃣ Save Payroll
        Payroll payroll = payrollRepository
                .findByEmpIdAndMonth(empId, month)
                .orElse(new Payroll());

        payroll.setEmpId(empId);
        payroll.setMonth(month);
        payroll.setBasicSalary(basic);
        payroll.setAllowances(allowances);
        payroll.setDeductions(totalDeductions);
        payroll.setNetSalary(netSalary);

        return payrollRepository.save(payroll);
    }
}