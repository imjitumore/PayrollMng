package com.project.payroll.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.payroll.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
