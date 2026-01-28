package com.project.payroll.service;

import com.project.payroll.entity.Attendance;
import com.project.payroll.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;

    public AttendanceService(AttendanceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Attendance save(Attendance attendance) {
        System.out.println("SERVICE SAVE: " + attendance.getEmpId() + " = " + attendance.getStatus());
        return repository.save(attendance);
    }

    public List<Attendance> getAll() {
        return repository.findAll();
    }

    public Attendance getByEmpAndDate(String empId, LocalDate date) {
        return repository.findByEmpIdAndDate(empId, date);
    }

    public List<Attendance> getByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    public List<Attendance> getByEmpAndMonth(String empId, LocalDate start, LocalDate end) {
        return repository.findByEmpIdAndDateBetween(empId, start, end);
    }
}
