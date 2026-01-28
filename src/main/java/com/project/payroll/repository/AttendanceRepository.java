package com.project.payroll.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.payroll.entity.Attendance;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByEmpIdAndDateBetween(
            String empId,
            LocalDate start,
            LocalDate end
    );

     List<Attendance> findByDate(LocalDate date);

    Attendance findByEmpIdAndDate(String empId, LocalDate date);
}
