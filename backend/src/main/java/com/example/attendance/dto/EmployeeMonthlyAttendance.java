package com.example.attendance.dto;

import java.util.List;

public record EmployeeMonthlyAttendance(
        Long employeeId,
        String employeeName,
        List<AttendanceResponse> records
) {
}
