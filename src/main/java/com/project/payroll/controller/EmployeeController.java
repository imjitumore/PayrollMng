package com.project.payroll.controller;

import com.project.payroll.entity.Employee;
import com.project.payroll.entity.User;
import com.project.payroll.service.EmployeeService;
import com.project.payroll.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;
    private final UserService userService;

    @Value("${app.default-password:company123}")
    private String defaultPassword;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public EmployeeController(EmployeeService service,
            UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    // LIST EMPLOYEES
    @GetMapping
    public String list(Model model) {
        model.addAttribute("employees", service.getAll());
        return "employees";
    }

    // ADD EMPLOYEE PAGE
    @GetMapping("/add")
    public String addEmployee(Model model) {
        model.addAttribute("employee", new Employee());
        return "employee-add";
    }

    // EDIT EMPLOYEE PAGE
    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable Long id, Model model) {
        Employee emp = service.getById(id).orElseThrow(() -> new IllegalArgumentException("Invalid employee Id:" + id));
        model.addAttribute("employee", emp);
        return "employee-add"; // reuse add-employee.html for edit
    }

    // SAVE (ADD OR EDIT)
    @PostMapping("/save")
    public String save(@ModelAttribute Employee emp) {
        service.save(emp); // creation of the corresponding User is handled in service
        return "redirect:/employees";
    }

    // DELETE EMPLOYEE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteById(id);
        return "redirect:/employees";
    }
}
