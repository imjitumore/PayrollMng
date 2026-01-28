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
    public String profilePage(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        Optional<User> userOpt = userRepository.findByUsername(sessionUser.getUsername());
        if (userOpt.isEmpty()) return "redirect:/login";

        model.addAttribute("user", userOpt.get());
        return "profile";
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
        if (sessionUser == null) return null;

        Optional<User> userOpt = userRepository.findByUsername(sessionUser.getUsername());
        if (userOpt.isEmpty()) return null;

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
