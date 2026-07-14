package com.example.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendance.domain.WorkDurationCalculator;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.EmployeeMonthlyAttendance;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Employee;
import com.example.attendance.exception.AttendanceValidationException;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkDurationCalculator workDurationCalculator;

    public AttendanceServiceImpl(AttendanceRecordRepository attendanceRecordRepository,
                                 EmployeeRepository employeeRepository,
                                 WorkDurationCalculator workDurationCalculator) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
        this.workDurationCalculator = workDurationCalculator;
    }

    @Override
    @Transactional
    public AttendanceResponse clockIn(Long employeeId) {
        LocalDate today = LocalDate.now(JST);
        attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .ifPresent(record -> {
                    throw new AttendanceValidationException("本日は既に出勤打刻されています");
                });

        AttendanceRecord record = AttendanceRecord.builder()
                .employeeId(employeeId)
                .workDate(today)
                .clockInTime(LocalTime.now(JST))
                .build();
        AttendanceRecord saved = attendanceRecordRepository.save(record);
        return toAttendanceResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse clockOut(Long employeeId) {
        LocalDate today = LocalDate.now(JST);
        AttendanceRecord record = attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new AttendanceValidationException("出勤打刻がされていません"));

        if (record.getClockOutTime() != null) {
            throw new AttendanceValidationException("本日は既に退勤打刻されています");
        }

        record.setClockOutTime(LocalTime.now(JST));
        AttendanceRecord saved = attendanceRecordRepository.save(record);
        return toAttendanceResponse(saved);
    }

    @Override
    public AttendanceResponse getTodayStatus(Long employeeId) {
        LocalDate today = LocalDate.now(JST);
        return attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .map(this::toAttendanceResponse)
                .orElse(new AttendanceResponse(today, null, null, null));
    }

    @Override
    public MonthlyAttendanceResponse getMyMonthlyAttendance(Long employeeId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdAndMonth(employeeId, startDate, endDate);

        List<AttendanceResponse> responses = records.stream()
                .map(this::toAttendanceResponse)
                .toList();

        return new MonthlyAttendanceResponse(year, month, responses);
    }

    @Override
    public List<EmployeeMonthlyAttendance> getAllMonthlyAttendance(int year, int month, Long employeeId) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        if (employeeId != null) {
            return getFilteredByEmployee(employeeId, startDate, endDate);
        }

        return getAllEmployeesAttendance(startDate, endDate);
    }

    private List<EmployeeMonthlyAttendance> getFilteredByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdAndMonth(employeeId, startDate, endDate);

        List<AttendanceResponse> responses = records.stream()
                .map(this::toAttendanceResponse)
                .toList();

        return List.of(new EmployeeMonthlyAttendance(employee.getId(), employee.getName(), responses));
    }

    private List<EmployeeMonthlyAttendance> getAllEmployeesAttendance(LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> allRecords = attendanceRecordRepository.findByMonth(startDate, endDate);
        List<Employee> employees = employeeRepository.findAll();

        Map<Long, List<AttendanceRecord>> grouped = allRecords.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getEmployeeId));

        return employees.stream()
                .filter(emp -> grouped.containsKey(emp.getId()))
                .map(emp -> {
                    List<AttendanceResponse> responses = grouped.get(emp.getId()).stream()
                            .map(this::toAttendanceResponse)
                            .toList();
                    return new EmployeeMonthlyAttendance(emp.getId(), emp.getName(), responses);
                })
                .toList();
    }

    private AttendanceResponse toAttendanceResponse(AttendanceRecord record) {
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
