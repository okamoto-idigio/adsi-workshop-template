package com.example.attendance.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.when;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.domain.WorkDurationCalculator;
import com.example.attendance.dto.EmployeeMonthlyAttendance;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.Role;
import com.example.attendance.repository.AttendanceRecordRepository;
import com.example.attendance.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private WorkDurationCalculator workDurationCalculator;

    private AttendanceServiceImpl service;

    @BeforeEach
    void setUp() {
        workDurationCalculator = new WorkDurationCalculator();
        service = new AttendanceServiceImpl(attendanceRecordRepository, employeeRepository, workDurationCalculator);
    }

    @Test
    @DisplayName("月次勤怠一覧: データありの月はレコードが返される")
    void getMyMonthlyAttendance_withRecords_returnsMonthlyData() {
        // Arrange
        Long employeeId = 1L;
        LocalDate startDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 31);

        var record = AttendanceRecord.builder()
                .employeeId(employeeId)
                .workDate(LocalDate.of(2026, 7, 1))
                .clockInTime(LocalTime.of(9, 0))
                .clockOutTime(LocalTime.of(18, 0))
                .build();

        when(attendanceRecordRepository.findByEmployeeIdAndMonth(employeeId, startDate, endDate))
                .thenReturn(List.of(record));

        // Act
        MonthlyAttendanceResponse result = service.getMyMonthlyAttendance(employeeId, 2026, 7);

        // Assert
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(7);
        assertThat(result.records()).hasSize(1);
        assertThat(result.records().getFirst().date()).isEqualTo(LocalDate.of(2026, 7, 1));
    }

    @Test
    @DisplayName("月次勤怠一覧: データなし月は空リスト")
    void getMyMonthlyAttendance_noRecords_returnsEmptyList() {
        // Arrange
        Long employeeId = 1L;
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);

        when(attendanceRecordRepository.findByEmployeeIdAndMonth(employeeId, startDate, endDate))
                .thenReturn(List.of());

        // Act
        MonthlyAttendanceResponse result = service.getMyMonthlyAttendance(employeeId, 2026, 8);

        // Assert
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.month()).isEqualTo(8);
        assertThat(result.records()).isEmpty();
    }

    @Test
    @DisplayName("月次勤怠一覧: 勤務時間が正しく計算される（昼休憩控除）")
    void getMyMonthlyAttendance_calculatesWorkDuration() {
        // Arrange
        Long employeeId = 1L;
        LocalDate startDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 31);

        var record = AttendanceRecord.builder()
                .employeeId(employeeId)
                .workDate(LocalDate.of(2026, 7, 1))
                .clockInTime(LocalTime.of(9, 0))
                .clockOutTime(LocalTime.of(18, 0))
                .build();

        when(attendanceRecordRepository.findByEmployeeIdAndMonth(employeeId, startDate, endDate))
                .thenReturn(List.of(record));

        // Act
        MonthlyAttendanceResponse result = service.getMyMonthlyAttendance(employeeId, 2026, 7);

        // Assert — 9:00-18:00 = 9h - 1h break = 8h = 480min
        assertThat(result.records().getFirst().workDurationMinutes()).isEqualTo(480);
    }

    @Test
    @DisplayName("全社員月次勤怠: 社員ごとにグルーピングされる")
    void getAllMonthlyAttendance_returnsGroupedByEmployee() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 31);

        var emp1 = Employee.builder().id(1L).username("tanaka").name("田中太郎").role(Role.EMPLOYEE).build();
        var emp2 = Employee.builder().id(2L).username("suzuki").name("鈴木花子").role(Role.EMPLOYEE).build();

        var record1 = AttendanceRecord.builder()
                .employeeId(1L).workDate(LocalDate.of(2026, 7, 1))
                .clockInTime(LocalTime.of(9, 0)).clockOutTime(LocalTime.of(18, 0)).build();
        var record2 = AttendanceRecord.builder()
                .employeeId(2L).workDate(LocalDate.of(2026, 7, 1))
                .clockInTime(LocalTime.of(10, 0)).clockOutTime(LocalTime.of(19, 0)).build();

        when(attendanceRecordRepository.findByMonth(startDate, endDate))
                .thenReturn(List.of(record1, record2));
        when(employeeRepository.findAll()).thenReturn(List.of(emp1, emp2));

        // Act
        List<EmployeeMonthlyAttendance> result = service.getAllMonthlyAttendance(2026, 7, null);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).employeeName()).isEqualTo("田中太郎");
        assertThat(result.get(1).employeeName()).isEqualTo("鈴木花子");
    }

    @Test
    @DisplayName("全社員月次勤怠: 社員IDフィルタで1名のみ返る")
    void getAllMonthlyAttendance_withEmployeeId_filtersToOneEmployee() {
        // Arrange
        Long targetEmployeeId = 1L;
        LocalDate startDate = LocalDate.of(2026, 7, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 31);

        var emp1 = Employee.builder().id(1L).username("tanaka").name("田中太郎").role(Role.EMPLOYEE).build();

        var record1 = AttendanceRecord.builder()
                .employeeId(1L).workDate(LocalDate.of(2026, 7, 1))
                .clockInTime(LocalTime.of(9, 0)).clockOutTime(LocalTime.of(18, 0)).build();

        when(attendanceRecordRepository.findByEmployeeIdAndMonth(targetEmployeeId, startDate, endDate))
                .thenReturn(List.of(record1));
        when(employeeRepository.findById(targetEmployeeId)).thenReturn(java.util.Optional.of(emp1));

        // Act
        List<EmployeeMonthlyAttendance> result = service.getAllMonthlyAttendance(2026, 7, targetEmployeeId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().employeeId()).isEqualTo(1L);
        assertThat(result.getFirst().employeeName()).isEqualTo("田中太郎");
    }
}
