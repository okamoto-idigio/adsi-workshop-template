package com.example.attendance.dto;

import java.util.List;

public record MonthlyAttendanceResponse(
        int year,
        int month,
        List<AttendanceResponse> records
) {
}
