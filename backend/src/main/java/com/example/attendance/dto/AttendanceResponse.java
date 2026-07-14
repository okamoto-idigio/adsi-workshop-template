package com.example.attendance.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceResponse(
        LocalDate date,
        LocalTime clockInTime,
        LocalTime clockOutTime,
        Integer workDurationMinutes
) {
}
