package com.example.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.attendance.entity.AttendanceRecord;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.employeeId = :employeeId " +
           "AND a.workDate >= :startDate AND a.workDate <= :endDate ORDER BY a.workDate")
    List<AttendanceRecord> findByEmployeeIdAndMonth(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.workDate >= :startDate " +
           "AND a.workDate <= :endDate ORDER BY a.employeeId, a.workDate")
    List<AttendanceRecord> findByMonth(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
