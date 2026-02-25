package com.project.payroll.controller;

import com.project.payroll.entity.User;
import com.project.payroll.service.AttendanceService;
import com.project.payroll.service.EmployeeService;
import com.project.payroll.service.PayrollService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final PayrollService payrollService;

    public DashboardController(EmployeeService employeeService,
            AttendanceService attendanceService,
            PayrollService payrollService) {
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
        this.payrollService = payrollService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        // if an employee is using the system, forward to the portal instead
        if (user.getEmpId() != null) {
            return "redirect:/portal";
        }

        model.addAttribute("totalEmployees", employeeService.getAll().size());
        model.addAttribute("totalAttendance", attendanceService.getAll().size());
        model.addAttribute("totalPayrolls", payrollService.getAll().size());

        return "dashboard";
    }
}
