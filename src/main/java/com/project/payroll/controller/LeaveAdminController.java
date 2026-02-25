package com.project.payroll.controller;

import com.project.payroll.entity.LeaveRequest;
import com.project.payroll.entity.User;
import com.project.payroll.service.LeaveRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/leaves")
public class LeaveAdminController {

    private final LeaveRequestService leaveService;

    public LeaveAdminController(LeaveRequestService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        // optionally check that user is admin (no empId)
        if (user.getEmpId() != null) {
            return "redirect:/portal";
        }
        List<LeaveRequest> requests = leaveService.getAll();
        model.addAttribute("leaves", requests);
        return "admin/leave-requests";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session) {
        leaveService.updateStatus(id, "Approved");
        return "redirect:/admin/leaves";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, HttpSession session) {
        leaveService.updateStatus(id, "Rejected");
        return "redirect:/admin/leaves";
    }
}


