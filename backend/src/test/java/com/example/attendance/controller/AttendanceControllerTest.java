package com.example.attendance.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.when;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.attendance.config.SessionUser;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.EmployeeMonthlyAttendance;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.entity.Role;
import com.example.attendance.exception.AttendanceValidationException;
import com.example.attendance.service.AttendanceService;

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    private MockHttpSession createSession(Long id, Role role) {
        var session = new MockHttpSession();
        session.setAttribute(SessionUser.SESSION_KEY, new SessionUser(id, "testuser", "テストユーザー", role));
        return session;
    }

    @Test
    @DisplayName("POST /api/attendance/clock-in - ログイン済みで出勤打刻成功")
    void clockIn_authenticated_returns200() throws Exception {
        var session = createSession(1L, Role.EMPLOYEE);
        var response = new AttendanceResponse(LocalDate.now(), LocalTime.of(9, 0), null, null);
        when(attendanceService.clockIn(1L)).thenReturn(response);

        mockMvc.perform(post("/api/attendance/clock-in")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockInTime").value("09:00:00"));
    }

    @Test
    @DisplayName("POST /api/attendance/clock-in - 未ログインで401")
    void clockIn_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/attendance/clock-out - ログイン済みで退勤打刻成功")
    void clockOut_authenticated_returns200() throws Exception {
        var session = createSession(1L, Role.EMPLOYEE);
        var response = new AttendanceResponse(LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(18, 0), 480);
        when(attendanceService.clockOut(1L)).thenReturn(response);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOutTime").value("18:00:00"))
                .andExpect(jsonPath("$.workDurationMinutes").value(480));
    }

    @Test
    @DisplayName("GET /api/attendance/status - ログイン済みで当日状態取得")
    void getTodayStatus_authenticated_returns200() throws Exception {
        var session = createSession(1L, Role.EMPLOYEE);
        var response = new AttendanceResponse(LocalDate.now(), LocalTime.of(9, 0), null, null);
        when(attendanceService.getTodayStatus(1L)).thenReturn(response);

        mockMvc.perform(get("/api/attendance/status")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockInTime").value("09:00:00"));
    }

    @Test
    @DisplayName("POST /api/attendance/clock-in - 出勤重複で400")
    void clockIn_alreadyClockedIn_returns400() throws Exception {
        var session = createSession(1L, Role.EMPLOYEE);
        when(attendanceService.clockIn(1L))
                .thenThrow(new AttendanceValidationException("本日は既に出勤打刻されています"));

        mockMvc.perform(post("/api/attendance/clock-in")
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/attendance/me: 認証済みユーザーは200を返す")
    void getMyAttendance_authenticated_returns200() throws Exception {
        var session = createSession(1L, Role.EMPLOYEE);
        var response = new MonthlyAttendanceResponse(2026, 7, List.of(
                new AttendanceResponse(LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(18, 0), 480)
        ));
        when(attendanceService.getMyMonthlyAttendance(1L, 2026, 7)).thenReturn(response);

        mockMvc.perform(get("/api/attendance/me")
                        .param("year", "2026")
                        .param("month", "7")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(7))
                .andExpect(jsonPath("$.records[0].workDurationMinutes").value(480));
    }

    @Test
    @DisplayName("GET /api/attendance/me: 未認証は401を返す")
    void getMyAttendance_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/attendance/me")
                        .param("year", "2026")
                        .param("month", "7"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/attendance/all: ADMINは200を返す")
    void getAllAttendance_admin_returns200() throws Exception {
        var session = createSession(1L, Role.ADMIN);
        var response = List.of(
                new EmployeeMonthlyAttendance(2L, "田中太郎", List.of(
                        new AttendanceResponse(LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(18, 0), 480)
                ))
        );
        when(attendanceService.getAllMonthlyAttendance(2026, 7, null)).thenReturn(response);

        mockMvc.perform(get("/api/attendance/all")
                        .param("year", "2026")
                        .param("month", "7")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeName").value("田中太郎"));
    }

    @Test
    @DisplayName("GET /api/attendance/all: EMPLOYEEは403を返す")
    void getAllAttendance_employee_returns403() throws Exception {
        var session = createSession(2L, Role.EMPLOYEE);

        mockMvc.perform(get("/api/attendance/all")
                        .param("year", "2026")
                        .param("month", "7")
                        .session(session))
                .andExpect(status().isForbidden());
    }
}
