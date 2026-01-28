package com.project.payroll.config;

import com.project.payroll.entity.User;
import com.project.payroll.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadUsers(UserRepository userRepository) {
        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User user = new User();
                user.setUsername("admin");
                user.setPassword("admin123");

                userRepository.save(user);

                System.out.println("Default admin user inserted");
            }
        };
    }
}
