package com.example.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendance.config.SessionUser;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.EmployeeMonthlyAttendance;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.entity.Role;
import com.example.attendance.service.AttendanceService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/attendance")
@Validated
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AttendanceResponse response = attendanceService.clockIn(user.id());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AttendanceResponse response = attendanceService.clockOut(user.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<AttendanceResponse> getTodayStatus(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AttendanceResponse response = attendanceService.getTodayStatus(user.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MonthlyAttendanceResponse> getMyAttendance(
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MonthlyAttendanceResponse response = attendanceService.getMyMonthlyAttendance(user.id(), year, month);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmployeeMonthlyAttendance>> getAllAttendance(
            @RequestParam @Min(2000) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam(required = false) Long employeeId,
            HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (user.role() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<EmployeeMonthlyAttendance> response = attendanceService.getAllMonthlyAttendance(year, month, employeeId);
        return ResponseEntity.ok(response);
    }
}
