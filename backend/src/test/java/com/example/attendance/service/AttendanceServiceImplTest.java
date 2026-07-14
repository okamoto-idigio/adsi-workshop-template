package com.example.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.domain.WorkDurationCalculator;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.exception.AttendanceValidationException;
import com.example.attendance.repository.AttendanceRecordRepository;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private WorkDurationCalculator workDurationCalculator;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @Test
    @DisplayName("当日未打刻の社員が出勤打刻すると出勤時刻が記録される")
    void clockIn_notYetClockedIn_createsRecord() {
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse result = attendanceService.clockIn(1L);

        assertThat(result.date()).isEqualTo(LocalDate.now());
        assertThat(result.clockInTime()).isNotNull();
        assertThat(result.clockOutTime()).isNull();
        assertThat(result.workDurationMinutes()).isNull();
    }

    @Test
    @DisplayName("同日に既に出勤済みの場合はAttendanceValidationExceptionが投げられる")
    void clockIn_alreadyClockedIn_throwsValidationException() {
        var existing = AttendanceRecord.builder()
                .employeeId(1L)
                .workDate(LocalDate.now())
                .clockInTime(LocalTime.of(9, 0))
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> attendanceService.clockIn(1L))
                .isInstanceOf(AttendanceValidationException.class);
    }

    @Test
    @DisplayName("出勤済みの社員が退勤打刻すると退勤時刻が記録される")
    void clockOut_clockedIn_recordsClockOutTime() {
        var existing = AttendanceRecord.builder()
                .employeeId(1L)
                .workDate(LocalDate.now())
                .clockInTime(LocalTime.of(9, 0))
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(existing));
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(workDurationCalculator.calculateWorkMinutes(any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(480);

        AttendanceResponse result = attendanceService.clockOut(1L);

        assertThat(result.date()).isEqualTo(LocalDate.now());
        assertThat(result.clockInTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.clockOutTime()).isNotNull();
        assertThat(result.workDurationMinutes()).isEqualTo(480);
    }

    @Test
    @DisplayName("未出勤の状態で退勤打刻するとAttendanceValidationExceptionが投げられる")
    void clockOut_notClockedIn_throwsValidationException() {
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.clockOut(1L))
                .isInstanceOf(AttendanceValidationException.class);
    }

    @Test
    @DisplayName("既に退勤済みの場合はAttendanceValidationExceptionが投げられる")
    void clockOut_alreadyClockedOut_throwsValidationException() {
        var existing = AttendanceRecord.builder()
                .employeeId(1L)
                .workDate(LocalDate.now())
                .clockInTime(LocalTime.of(9, 0))
                .clockOutTime(LocalTime.of(18, 0))
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> attendanceService.clockOut(1L))
                .isInstanceOf(AttendanceValidationException.class);
    }

    @Test
    @DisplayName("当日レコードがない場合はnullフィールドのレスポンスが返される")
    void getTodayStatus_noRecord_returnsEmptyResponse() {
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());

        AttendanceResponse result = attendanceService.getTodayStatus(1L);

        assertThat(result.date()).isEqualTo(LocalDate.now());
        assertThat(result.clockInTime()).isNull();
        assertThat(result.clockOutTime()).isNull();
        assertThat(result.workDurationMinutes()).isNull();
    }
}
