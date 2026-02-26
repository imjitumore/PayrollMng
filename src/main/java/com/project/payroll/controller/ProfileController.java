package com.project.payroll.controller;

import com.project.payroll.entity.User;
import com.project.payroll.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // SHOW PROFILE PAGE
    @GetMapping
    public String profilePage(
            @RequestParam(value = "empId", required = false) String empIdParam,
            HttpSession session,
            Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        User userToDisplay = null;
        boolean viewingEmployee = false;

        // If empId parameter is provided (from employee portal), show that employee's profile
        if (empIdParam != null && !empIdParam.isEmpty()) {
            viewingEmployee = true;
            // Find user by empId
            userToDisplay = findUserByEmpId(empIdParam);
            if (userToDisplay == null) {
                // If not found by empId, try by username (backward compatibility)
                Optional<User> userOpt = userRepository.findByUsername(empIdParam);
                if (userOpt.isEmpty()) {
                    return "redirect:/login";
                }
                userToDisplay = userOpt.get();
            }
        } else {
            // Otherwise show current logged-in user's profile
            Optional<User> userOpt = userRepository.findByUsername(sessionUser.getUsername());
            if (userOpt.isEmpty()) {
                return "redirect:/login";
            }
            userToDisplay = userOpt.get();
        }

        // whether the logged‑in user is an employee (empId set) helps template choose portal vs admin sidebar
        boolean currentUserIsEmployee = sessionUser.getEmpId() != null;
        model.addAttribute("user", userToDisplay);
        model.addAttribute("viewingEmployee", viewingEmployee);
        model.addAttribute("currentUserIsEmployee", currentUserIsEmployee);
        return "profile";
    }

    // Helper method to find user by empId
    private User findUserByEmpId(String empId) {
        // Try to find user by empId using JPQL or Criteria API
        // Since we don't have a direct method, we'll use findAll and filter
        // In a real scenario, add a findByEmpId method to UserRepository
        try {
            return userRepository.findAll().stream()
                    .filter(user -> user.getEmpId() != null && user.getEmpId().equals(empId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    // UPDATE PROFILE via AJAX
    @PostMapping("/update")
    @ResponseBody
    public User updateProfile(
            @RequestParam("contact") String contact,
            @RequestParam("address") String address,
            @RequestParam(value = "image", required = false) MultipartFile image,
            HttpSession session
    ) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return null;
        }

        Optional<User> userOpt = userRepository.findByUsername(sessionUser.getUsername());
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        user.setContact(contact);
        user.setAddress(address);

        try {
            if (image != null && !image.isEmpty()) {
                String uploadDir = "uploads/profile/";
                Files.createDirectories(Paths.get(uploadDir));

                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                image.transferTo(filePath.toFile());

                user.setProfileImage(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        userRepository.save(user);
        session.setAttribute("user", user);
        return user; // JSON response
    }
}
