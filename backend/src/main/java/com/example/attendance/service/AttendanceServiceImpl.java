package com.example.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.domain.WorkDurationCalculator;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.EmployeeMonthlyAttendance;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.exception.AttendanceValidationException;
import com.example.attendance.repository.AttendanceRecordRepository;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final WorkDurationCalculator workDurationCalculator;

    public AttendanceServiceImpl(AttendanceRecordRepository attendanceRecordRepository,
                                 WorkDurationCalculator workDurationCalculator) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.workDurationCalculator = workDurationCalculator;
    }

    @Override
    @Transactional
    public AttendanceResponse clockIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .ifPresent(record -> {
                    throw new AttendanceValidationException("本日は既に出勤打刻されています");
                });

        AttendanceRecord record = AttendanceRecord.builder()
                .employeeId(employeeId)
                .workDate(today)
                .clockInTime(LocalTime.now())
                .build();
        AttendanceRecord saved = attendanceRecordRepository.save(record);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse clockOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new AttendanceValidationException("出勤打刻がされていません"));

        if (record.getClockOutTime() != null) {
            throw new AttendanceValidationException("本日は既に退勤打刻されています");
        }

        record.setClockOutTime(LocalTime.now());
        AttendanceRecord saved = attendanceRecordRepository.save(record);
        return toResponse(saved);
    }

    @Override
    public AttendanceResponse getTodayStatus(Long employeeId) {
        LocalDate today = LocalDate.now();
        return attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .map(this::toResponse)
                .orElse(new AttendanceResponse(today, null, null, null));
    }

    @Override
    public MonthlyAttendanceResponse getMyMonthlyAttendance(Long employeeId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<AttendanceRecord> records = attendanceRecordRepository.findByEmployeeIdAndMonth(employeeId, startDate, endDate);
        List<AttendanceResponse> responses = records.stream().map(this::toResponse).toList();
        return new MonthlyAttendanceResponse(year, month, responses);
    }

    @Override
    public List<EmployeeMonthlyAttendance> getAllMonthlyAttendance(int year, int month, Long employeeId) {
        throw new UnsupportedOperationException("Unit 02 で実装予定");
    }

    private AttendanceResponse toResponse(AttendanceRecord record) {
        Integer workMinutes = null;
        if (record.getClockInTime() != null && record.getClockOutTime() != null) {
            workMinutes = workDurationCalculator.calculateWorkMinutes(
                    record.getClockInTime(), record.getClockOutTime());
        }
        return new AttendanceResponse(
                record.getWorkDate(),
                record.getClockInTime(),
                record.getClockOutTime(),
                workMinutes
        );
    }
}
