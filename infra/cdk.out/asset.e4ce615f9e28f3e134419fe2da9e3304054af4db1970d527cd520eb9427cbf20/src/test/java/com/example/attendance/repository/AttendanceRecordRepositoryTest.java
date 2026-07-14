package com.example.attendance.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.example.attendance.entity.AttendanceRecord;

@DataJpaTest
@ActiveProfiles("test")
class AttendanceRecordRepositoryTest {

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("保存した勤怠記録をemployeeIdとdateで取得できる")
    void save_andFindByEmployeeIdAndWorkDate_returnsRecord() {
        AttendanceRecord record = AttendanceRecord.builder()
                .employeeId(2L)
                .workDate(LocalDate.of(2026, 7, 14))
                .clockInTime(LocalTime.of(9, 0))
                .build();

        attendanceRecordRepository.save(record);
        entityManager.flush();
        entityManager.clear();

        Optional<AttendanceRecord> result = attendanceRecordRepository
                .findByEmployeeIdAndWorkDate(2L, LocalDate.of(2026, 7, 14));

        assertThat(result).isPresent();
        assertThat(result.get().getClockInTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    @DisplayName("同一社員・同一日のレコードを2件保存するとユニーク制約違反")
    void save_duplicateEmployeeAndDate_throwsException() {
        AttendanceRecord record1 = AttendanceRecord.builder()
                .employeeId(2L)
                .workDate(LocalDate.of(2026, 7, 14))
                .clockInTime(LocalTime.of(9, 0))
                .build();
        attendanceRecordRepository.save(record1);
        entityManager.flush();

        AttendanceRecord record2 = AttendanceRecord.builder()
                .employeeId(2L)
                .workDate(LocalDate.of(2026, 7, 14))
                .clockInTime(LocalTime.of(10, 0))
                .build();

        assertThatThrownBy(() -> {
            attendanceRecordRepository.save(record2);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }
}
