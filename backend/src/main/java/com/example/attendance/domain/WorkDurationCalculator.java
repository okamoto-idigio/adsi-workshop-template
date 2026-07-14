package com.example.attendance.domain;

import java.time.Duration;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

@Component
public class WorkDurationCalculator {

    private static final LocalTime BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime BREAK_END = LocalTime.of(13, 0);
    private static final int BREAK_DURATION_MINUTES = 60;

    public int calculateWorkMinutes(LocalTime clockIn, LocalTime clockOut) {
        long totalMinutes = Duration.between(clockIn, clockOut).toMinutes();
        int breakMinutes = getBreakMinutes(clockIn, clockOut);
        return (int) (totalMinutes - breakMinutes);
    }

    public int getBreakMinutes(LocalTime clockIn, LocalTime clockOut) {
        if (clockIn.isBefore(BREAK_END) && clockOut.isAfter(BREAK_START)) {
            return BREAK_DURATION_MINUTES;
        }
        return 0;
    }
}
