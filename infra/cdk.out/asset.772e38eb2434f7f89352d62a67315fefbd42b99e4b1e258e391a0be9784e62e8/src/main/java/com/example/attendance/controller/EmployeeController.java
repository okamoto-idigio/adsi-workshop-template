package com.example.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.config.SessionUser;
import com.example.attendance.dto.EmployeeSummary;
import com.example.attendance.entity.Role;
import com.example.attendance.service.EmployeeService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeSummary>> getEmployees(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.role() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<EmployeeSummary> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
}
