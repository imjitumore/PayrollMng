package com.project.payroll.controller;

import com.project.payroll.entity.Employee;
import com.project.payroll.entity.Attendance;
import com.project.payroll.entity.Payroll;
import com.project.payroll.repository.EmployeeRepository;
import com.project.payroll.repository.AttendanceRepository;
import com.project.payroll.repository.PayrollRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    private final EmployeeRepository employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final PayrollRepository payrollRepo;

    public ReportsController(EmployeeRepository employeeRepo,
                             AttendanceRepository attendanceRepo,
                             PayrollRepository payrollRepo) {
        this.employeeRepo = employeeRepo;
        this.attendanceRepo = attendanceRepo;
        this.payrollRepo = payrollRepo;
    }

    // ================= DASHBOARD =================
    @GetMapping
    public String reportsDashboard(
            @RequestParam(required = false) String activeTab,
            Model model) {

        String tab = (activeTab != null) ? activeTab : "employees";

        if ("employees".equals(tab)) {
            model.addAttribute("employees", employeeRepo.findAll());
        } else {
            model.addAttribute("employees", Collections.emptyList());
        }

        model.addAttribute("monthView", Collections.emptyList());
        model.addAttribute("payroll", null);
        model.addAttribute("selectedEmpId", "");
        model.addAttribute("selectedMonth", "");
        model.addAttribute("activeTab", tab);

        return "reports/reports-dashboard";
    }

    // ================= MONTHLY ATTENDANCE =================
    @GetMapping("/attendance")
    public String monthlyAttendance(@RequestParam String empId,
                                    @RequestParam String month,
                                    Model model) {

        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Attendance> records =
                attendanceRepo.findByEmpIdAndDateBetween(empId, start, end);

        Map<LocalDate, String> statusMap = new HashMap<>();
        for (Attendance a : records) {
            statusMap.put(a.getDate(), a.getStatus());
        }

        List<Map<String, String>> monthView = new ArrayList<>();

        for (int i = 1; i <= ym.lengthOfMonth(); i++) {

            LocalDate date = ym.atDay(i);
            Map<String, String> row = new HashMap<>();

            String status;

            // ✅ Sunday Auto Holiday
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                status = "Holiday";
            } else {
                status = statusMap.getOrDefault(date, "Pending");
            }

            row.put("date", date.toString());
            row.put("status", status);

            monthView.add(row);
        }

        model.addAttribute("monthView", monthView);
        model.addAttribute("selectedEmpId", empId);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("employees", Collections.emptyList());
        model.addAttribute("payroll", null);
        model.addAttribute("activeTab", "attendance");

        return "reports/reports-dashboard";
    }

    // ================= SALARY SLIP =================
    @GetMapping("/salary")
    public String salarySlip(@RequestParam String empId,
                             @RequestParam String month,
                             Model model) {

        // ✅ USE findByEmpId (Correct for your structure)
        Optional<Employee> empOpt = employeeRepo.findByEmpId(empId);

        if (empOpt.isEmpty()) {
            model.addAttribute("error", "Employee not found");
            return "redirect:/reports?activeTab=salary";
        }

        Employee emp = empOpt.get();

        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Attendance> records =
                attendanceRepo.findByEmpIdAndDateBetween(empId, start, end);

        int absentDays = 0;

        for (Attendance a : records) {
            if ("Absent".equalsIgnoreCase(a.getStatus())) {
                absentDays++;
            }
        }

        double basicSalary = emp.getBasicSalary();
        double dailySalary = basicSalary / 30.0;

        double deductions = absentDays * dailySalary;
        double allowances = basicSalary * 0.20;
        double netSalary = basicSalary + allowances - deductions;

        Payroll payroll = new Payroll();
        payroll.setEmpId(empId);
        payroll.setMonth(month);
        payroll.setBasicSalary(basicSalary);
        payroll.setAllowances(allowances);
        payroll.setDeductions(deductions);
        payroll.setNetSalary(netSalary);

        model.addAttribute("payroll", payroll);
        model.addAttribute("employees", Collections.emptyList());
        model.addAttribute("monthView", Collections.emptyList());
        model.addAttribute("selectedEmpId", empId);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("activeTab", "salary");

        return "reports/reports-dashboard";
    }

}