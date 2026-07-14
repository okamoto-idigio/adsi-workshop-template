package com.example.attendance.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.attendance.domain.WorkDurationCalculator;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.EmployeeMonthlyAttendance;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Employee;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;

@Service
public class AttendanceServiceImpl implements AttendanceService {

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
    public AttendanceResponse clockIn(Long employeeId) {
        throw new UnsupportedOperationException("Implemented in unit_01");
    }

    @Override
    public AttendanceResponse clockOut(Long employeeId) {
        throw new UnsupportedOperationException("Implemented in unit_01");
    }

    @Override
    public AttendanceResponse getTodayStatus(Long employeeId) {
        throw new UnsupportedOperationException("Implemented in unit_01");
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
