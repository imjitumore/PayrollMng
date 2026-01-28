package com.project.payroll.controller;

import com.project.payroll.entity.User;
import com.project.payroll.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ================= SIGNUP PAGE =================
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // ================= SIGNUP SUBMIT =================
    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        // username exists check
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "signup";
        }

        // password match check
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "signup";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);   // plain password
        // user.setEnabled(true);

        userRepository.save(user);

        model.addAttribute("success", "Account created successfully. Please login.");
        return "login";
    }

    // ================= LOGIN PAGE =================
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ================= LOGIN SUBMIT =================
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        session.setAttribute("user", user);
        return "redirect:/dashboard";
    }

    // ================= LOGOUT =================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
