package com.project.payroll.controller;

import com.project.payroll.entity.Payroll;
import com.project.payroll.service.PayrollService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // ================= PAGE LOAD =================
    @GetMapping
    public String payrollPage(Model model) {
        model.addAttribute("payroll", null);
        model.addAttribute("error", null);
        return "payroll";
    }

    // ================= GENERATE =================
    @PostMapping("/generate")
    public String generate(
            @RequestParam String empId,
            @RequestParam String month,
            Model model) {

        Payroll payroll = payrollService.generateSalarySlip(empId, month);

        if (payroll == null) {
            model.addAttribute("error", "Employee ID not found");
            model.addAttribute("payroll", null);
        } else {
            model.addAttribute("payroll", payroll);
            model.addAttribute("error", null);
        }

        return "payroll";
    }
}
