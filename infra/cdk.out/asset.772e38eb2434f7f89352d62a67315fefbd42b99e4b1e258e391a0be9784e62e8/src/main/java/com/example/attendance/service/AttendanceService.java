package com.example.attendance.service;

import java.util.List;

import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.EmployeeMonthlyAttendance;
import com.example.attendance.dto.MonthlyAttendanceResponse;

public interface AttendanceService {

    AttendanceResponse clockIn(Long employeeId);

    AttendanceResponse clockOut(Long employeeId);

    AttendanceResponse getTodayStatus(Long employeeId);

    MonthlyAttendanceResponse getMyMonthlyAttendance(Long employeeId, int year, int month);

    List<EmployeeMonthlyAttendance> getAllMonthlyAttendance(int year, int month, Long employeeId);
}
