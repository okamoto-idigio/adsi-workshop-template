package com.example.attendance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.config.SessionAuthFilter;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.service.AttendanceService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(HttpSession session) {
        Long employeeId = (Long) session.getAttribute(SessionAuthFilter.SESSION_USER_ID);
        AttendanceResponse response = attendanceService.clockIn(employeeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(HttpSession session) {
        Long employeeId = (Long) session.getAttribute(SessionAuthFilter.SESSION_USER_ID);
        AttendanceResponse response = attendanceService.clockOut(employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<AttendanceResponse> getTodayStatus(HttpSession session) {
        Long employeeId = (Long) session.getAttribute(SessionAuthFilter.SESSION_USER_ID);
        AttendanceResponse response = attendanceService.getTodayStatus(employeeId);
        return ResponseEntity.ok(response);
    }
}
