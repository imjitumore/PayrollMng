package com.project.payroll.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.project.payroll.entity.User;
import com.project.payroll.repository.UserRepository;

@Configuration
public class DataLoader {

    @Value("${app.default-password:company123}")
    private String defaultPassword;

    @Bean
    CommandLineRunner loadUsers(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User user = new User();
                user.setUsername("admin");
                user.setPassword(encoder.encode(defaultPassword));
                user.setPasswordChanged(true); // admin doesn't need to change

                userRepository.save(user);

                System.out.println("Default admin user inserted");
            }
        };
    }
}
