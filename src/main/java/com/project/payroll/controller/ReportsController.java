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

            // Sunday Auto Holiday
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

        Optional<Payroll> payrollOpt =
                payrollRepo.findByEmpIdAndMonth(empId, month);

        if (payrollOpt.isEmpty()) {
            model.addAttribute("error",
                    "Payroll not generated for this employee and month.");
            model.addAttribute("payroll", null);
        } else {
            model.addAttribute("payroll", payrollOpt.get());
        }

        model.addAttribute("employees", Collections.emptyList());
        model.addAttribute("monthView", Collections.emptyList());
        model.addAttribute("selectedEmpId", empId);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("activeTab", "salary");

        return "reports/reports-dashboard";
    }

    // ================= DOWNLOAD SALARY PDF =================
    @GetMapping("/salary/pdf")
    public ResponseEntity<byte[]> downloadSalaryPdf(
            @RequestParam String empId,
            @RequestParam String month) {

        Optional<Payroll> payrollOpt =
                payrollRepo.findByEmpIdAndMonth(empId, month);

        if (payrollOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Payroll payroll = payrollOpt.get();

        String htmlContent = generateSalaryHtml(payroll);
        byte[] contents = htmlContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment",
                "salary-slip-" + empId + "-" + month + ".pdf");

        return new ResponseEntity<>(contents, headers, HttpStatus.OK);
    }

    // ================= HTML GENERATOR =================
    private String generateSalaryHtml(Payroll payroll) {

        return """
            <html>
            <body style="font-family:Arial;padding:40px;">
                <h2>Salary Slip</h2>
                <hr>
                <p><strong>Employee ID:</strong> %s</p>
                <p><strong>Month:</strong> %s</p>
                <p><strong>Basic Salary:</strong> ₹%.2f</p>
                <p><strong>Allowances:</strong> ₹%.2f</p>
                <p><strong>Deductions:</strong> ₹%.2f</p>
                <hr>
                <h3>Net Salary: ₹%.2f</h3>
                <br>
                <p>Generated on: %s</p>
            </body>
            </html>
            """.formatted(
                payroll.getEmpId(),
                payroll.getMonth(),
                payroll.getBasicSalary(),
                payroll.getAllowances(),
                payroll.getDeductions(),
                payroll.getNetSalary(),
                LocalDate.now()
        );
    }
}