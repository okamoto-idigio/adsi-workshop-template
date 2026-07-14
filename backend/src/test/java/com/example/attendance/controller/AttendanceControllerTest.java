package com.example.attendance.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.attendance.config.SessionAuthFilter;
import com.example.attendance.config.WebConfig;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.exception.AttendanceValidationException;
import com.example.attendance.service.AttendanceService;

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(WebConfig.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    private MockHttpSession authenticatedSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SessionAuthFilter.SESSION_USER_ID, 1L);
        return session;
    }

    @Test
    @DisplayName("POST /api/attendance/clock-in - ログイン済みで出勤打刻成功")
    void clockIn_authenticated_returns200() throws Exception {
        var response = new AttendanceResponse(LocalDate.now(), LocalTime.of(9, 0), null, null);
        when(attendanceService.clockIn(1L)).thenReturn(response);

        mockMvc.perform(post("/api/attendance/clock-in")
                        .session(authenticatedSession()))
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
        var response = new AttendanceResponse(LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(18, 0), 480);
        when(attendanceService.clockOut(1L)).thenReturn(response);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOutTime").value("18:00:00"))
                .andExpect(jsonPath("$.workDurationMinutes").value(480));
    }

    @Test
    @DisplayName("GET /api/attendance/status - ログイン済みで当日状態取得")
    void getTodayStatus_authenticated_returns200() throws Exception {
        var response = new AttendanceResponse(LocalDate.now(), LocalTime.of(9, 0), null, null);
        when(attendanceService.getTodayStatus(1L)).thenReturn(response);

        mockMvc.perform(get("/api/attendance/status")
                        .session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockInTime").value("09:00:00"));
    }

    @Test
    @DisplayName("POST /api/attendance/clock-in - 出勤重複で400")
    void clockIn_alreadyClockedIn_returns400() throws Exception {
        when(attendanceService.clockIn(1L))
                .thenThrow(new AttendanceValidationException("本日は既に出勤打刻されています"));

        mockMvc.perform(post("/api/attendance/clock-in")
                        .session(authenticatedSession()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ATTENDANCE_VALIDATION_ERROR"));
    }
}
