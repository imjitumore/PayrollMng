package com.project.payroll.controller;

import com.project.payroll.entity.Attendance;
import com.project.payroll.entity.LeaveRequest;
import com.project.payroll.entity.Payroll;
import com.project.payroll.entity.User;
import com.project.payroll.service.AttendanceService;
import com.project.payroll.service.LeaveRequestService;
import com.project.payroll.service.PayrollService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/portal")
public class EmployeePortalController {

    private final AttendanceService attendanceService;
    private final LeaveRequestService leaveService;
    private final PayrollService payrollService;

    public EmployeePortalController(AttendanceService attendanceService,
            LeaveRequestService leaveService,
            PayrollService payrollService) {
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
        this.payrollService = payrollService;
    }

    // landing page for employee after login
    @GetMapping
    public String portal(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String empId = user.getEmpId();
        if (empId == null) {
            // not an employee - redirect to admin dashboard
            return "redirect:/dashboard";
        }

        // current month attendance count
        YearMonth ym = YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int count = attendanceService.getByEmpAndMonth(empId, start, end).size();
        model.addAttribute("myCurrentMonthAttendance", count);
        model.addAttribute("empId", empId);

        return "portal/home";
    }

    // ======== leave application ========
    @GetMapping("/apply-leave")
    public String leaveForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("empId", user.getEmpId());
        model.addAttribute("leave", new LeaveRequest());
        return "portal/leave-apply";
    }

    @PostMapping("/apply-leave")
    public String submitLeave(@ModelAttribute LeaveRequest leave, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        leave.setEmpId(user.getEmpId());
        leave.setStatus("Pending");
        leaveService.save(leave);
        return "redirect:/portal/leave-list";
    }

    @GetMapping("/leave-list")
    public String leaveList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String empId = user.getEmpId();
        List<LeaveRequest> list = leaveService.getByEmpId(empId);
        model.addAttribute("leaves", list);
        model.addAttribute("empId", empId);
        return "portal/leave-list";
    }

    // ======== attendance status view ========
    @GetMapping("/attendance-status")
    public String attendanceStatusForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("empId", user.getEmpId());
        return "portal/attendance-status";
    }

    @PostMapping("/attendance-status")
    public String attendanceStatus(
            @RequestParam String month,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String empId = user.getEmpId();
        model.addAttribute("empId", empId);

        YearMonth ym = YearMonth.parse(month);
        List<LocalDate> days = new ArrayList<>();
        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            days.add(ym.atDay(i));
        }

        LocalDate today = LocalDate.now();
        List<DayStatus> statusList = new ArrayList<>();
        for (LocalDate d : days) {
            String status = "Pending";  // default status
            Attendance att = attendanceService.getByEmpAndDate(empId, d);
            if (att != null) {
                if ("Present".equalsIgnoreCase(att.getStatus())) {
                    status = "Present";
                } else if ("Leave".equalsIgnoreCase(att.getStatus())) {
                    // check leave request status for the day
                    List<LeaveRequest> overlap = leaveService.findOverlapping(empId, d);
                    if (!overlap.isEmpty()) {
                        status = overlap.get(0).getStatus();
                    } else {
                        status = "Leave";
                    }
                } else if ("Absent".equalsIgnoreCase(att.getStatus())) {
                    // admin explicitly set as absent
                    status = "Absent";
                } else {
                    status = att.getStatus();
                }
            } else {
                // if no attendance record but maybe there is a leave request pending/approved
                List<LeaveRequest> overlap = leaveService.findOverlapping(empId, d);
                if (!overlap.isEmpty()) {
                    status = overlap.get(0).getStatus();
                } else {
                    // no attendance and no leave
                    if (d.isBefore(today)) {
                        // past date without attendance record = Not Filled
                        status = "Not Filled";
                    } else {
                        // today or future date = Pending
                        status = "Pending";
                    }
                }
            }
            statusList.add(new DayStatus(d, status));
        }
        model.addAttribute("dayStatuses", statusList);
        return "portal/attendance-status";
    }

    // ======== salary slip (reuse existing /payroll but pre-populate) ========
    @GetMapping("/salary")
    public String salaryPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("empId", user.getEmpId());
        model.addAttribute("payroll", null);
        model.addAttribute("error", null);
        return "portal/salary";
    }

    @PostMapping("/salary/generate")
    public String salaryGenerate(@RequestParam String month,
            HttpSession session,
            Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String empId = user.getEmpId();
        model.addAttribute("empId", empId);

        Payroll payroll = payrollService.generateSalarySlip(empId, month);
        if (payroll == null) {
            model.addAttribute("error", "Employee ID not found");
            model.addAttribute("payroll", null);
        } else {
            model.addAttribute("payroll", payroll);
            model.addAttribute("error", null);
        }
        return "portal/salary";
    }

    // helper class to transport date + status
    public static class DayStatus {

        private LocalDate date;
        private String status;

        public DayStatus(LocalDate date, String status) {
            this.date = date;
            this.status = status;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }
    }
}
