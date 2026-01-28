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

    @GetMapping("/attendance")
    public String monthlyAttendance(@RequestParam String empId,
                                   @RequestParam String month,
                                   Model model) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Attendance> records = attendanceRepo.findByEmpIdAndDateBetween(empId, start, end);
        List<Map<String, String>> monthView = new ArrayList<>();
        
        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            LocalDate date = ym.atDay(i);
            Map<String, String> row = new HashMap<>();
            String status = "Pending";
            for (Attendance a : records) {
                if (a.getDate().equals(date)) {
                    status = a.getStatus();
                    break;
                }
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

    @GetMapping("/salary")
    public String salarySlip(@RequestParam String empId, 
                            @RequestParam String month, 
                            Model model) {
        Optional<Payroll> payrollOpt = payrollRepo.findByEmpIdAndMonth(empId, month);
        
        model.addAttribute("payroll", payrollOpt.orElse(null));
        model.addAttribute("employees", Collections.emptyList());
        model.addAttribute("monthView", Collections.emptyList());
        model.addAttribute("selectedEmpId", empId);
        model.addAttribute("selectedMonth", month);
        model.addAttribute("activeTab", "salary");
        return "reports/reports-dashboard";
    }

    @GetMapping("/salary/pdf")
    public ResponseEntity<byte[]> downloadSalaryPdf(
            @RequestParam String empId,
            @RequestParam String month) {
        
        Optional<Payroll> payrollOpt = payrollRepo.findByEmpIdAndMonth(empId, month);
        if (payrollOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Payroll payroll = payrollOpt.get();
        String htmlContent = generateSalaryHtml(payroll, empId, month);
        byte[] contents = htmlContent.getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "salary-slip-" + empId + "-" + month + ".pdf");
        
        return new ResponseEntity<>(contents, headers, HttpStatus.OK);
    }

    private String generateSalaryHtml(Payroll payroll, String empId, String month) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Salary Slip - %s</title>
                <style>
                    *{margin:0;padding:0;box-sizing:border-box;}
                    body{font-family:Arial,sans-serif;background:#f8fafc;padding:30px;}
                    .slip{max-width:600px;margin:0 auto;background:white;padding:40px;border-radius:12px;box-shadow:0 10px 40px rgba(0,0,0,0.1);}
                    .header{background:linear-gradient(135deg,#3b82f6 0%%,#1d4ed8 100%%);color:white;padding:30px;text-align:center;border-radius:12px 12px 0 0;}
                    .header h2{margin:0;font-size:28px;}
                    table{width:100%%;border-collapse:collapse;margin:25px 0;}
                    th,td{padding:16px;text-align:left;border-bottom:1px solid #e5e7eb;}
                    th{background:#f8fafc;font-weight:600;width:40%%;}
                    .net-row{background:linear-gradient(135deg,#10b981 0%%,#059669 100%%)!important;color:white!important;}
                    .net-row th,.net-row td{font-weight:bold;font-size:1.2em;}
                    .footer{text-align:center;margin-top:30px;color:#6b7280;font-size:14px;}
                    @media print{.slip{box-shadow:none;border:1px solid #ddd;}}
                </style>
            </head>
            <body>
                <div class="slip">
                    <div class="header">
                        <h2>Salary Slip</h2>
                        <p style="margin:5px 0 0;font-size:16px;opacity:0.95;">ABC Technologies Pvt. Ltd.</p>
                    </div>
                    <table>
                        <tr><th>Employee ID</th><td>%s</td></tr>
                        <tr><th>Month</th><td>%s</td></tr>
                        <tr><th>Basic Salary</th><td>₹%.2f</td></tr>
                        <tr><th>Allowances (20%%)</th><td>₹%.2f</td></tr>
                        <tr><th>Deductions (10%%)</th><td>₹%.2f</td></tr>
                        <tr class="net-row"><th>Net Salary</th><td>₹%.2f</td></tr>
                    </table>
                    <div class="footer">
                        <p>Generated on: %s</p>
                        <p><strong>Authorized Signatory</strong></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
            payroll.getEmpId(),
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
