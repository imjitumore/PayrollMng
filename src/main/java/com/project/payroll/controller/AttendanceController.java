package com.project.payroll.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.payroll.entity.Attendance;
import com.project.payroll.service.AttendanceService;
import com.project.payroll.service.EmployeeService;
@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;

    public AttendanceController(EmployeeService employeeService,
                                AttendanceService attendanceService) {
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public String attendancePage(
            @RequestParam(required = false) String empId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String date,
            Model model) {

        LocalDate selectedDate =
                (date != null && !date.isBlank())
                        ? LocalDate.parse(date)
                        : LocalDate.now();

        model.addAttribute("today", selectedDate.toString());
        model.addAttribute("employees", employeeService.getAll());

        Map<String, Attendance> attendanceMap = new HashMap<>();
        for (Attendance a : attendanceService.getByDate(selectedDate)) {
            attendanceMap.put(a.getEmpId(), a);
        }
        model.addAttribute("attendanceMap", attendanceMap);

        // ================= MONTH VIEW =================
        if (empId != null && month != null && !empId.isBlank()) {

            YearMonth ym = YearMonth.parse(month);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            List<Attendance> records =
                    attendanceService.getByEmpAndMonth(empId, start, end);

            List<Map<String, String>> monthView = new ArrayList<>();
            Map<LocalDate, String> statusMap = new HashMap<>();

            for (Attendance a : records) {
                statusMap.put(a.getDate(), a.getStatus());
            }

            for (int i = 1; i <= ym.lengthOfMonth(); i++) {
                LocalDate d = ym.atDay(i);
                Map<String, String> row = new HashMap<>();
                row.put("date", d.toString());
                row.put("status", statusMap.getOrDefault(d, "Pending"));
                monthView.add(row);
            }

            model.addAttribute("monthView", monthView);
            model.addAttribute("selectedEmpId", empId);
            model.addAttribute("selectedMonth", month);
        }

        return "attendance";
    }

   @PostMapping("/save")
public String saveAttendance(
        @RequestParam(value = "empId", required = false) List<String> empIds,
        @RequestParam(value = "employeeName", required = false) List<String> employeeNames,
        @RequestParam(value = "status", required = false) List<String> statuses,
        @RequestParam("date") String date) {

    System.out.println("=== DEBUG SAVE ===");
    System.out.println("empIds: " + empIds);
    System.out.println("statuses: " + statuses);
    
    LocalDate selectedDate = LocalDate.parse(date);
    
    if (empIds != null && statuses != null) {
        for (int i = 0; i < empIds.size(); i++) {
            if (i < statuses.size() && statuses.get(i) != null && !statuses.get(i).isEmpty()) {
                Attendance att = new Attendance();
                att.setEmpId(empIds.get(i));
                att.setEmployeeName(employeeNames.get(i));
                att.setStatus(statuses.get(i));
                att.setDate(selectedDate);
                
                attendanceService.save(att);  // Spring Boot auto-commits
                System.out.println("SAVED: " + empIds.get(i) + " = " + statuses.get(i));
            }
        }
    }
    
    return "redirect:/attendance?date=" + date;
}

}
