package com.example.attendance.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.attendance.dto.LoginResponse;
import com.example.attendance.entity.Role;
import com.example.attendance.exception.EmployeeNotFoundException;
import com.example.attendance.service.AuthService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login - 正常ログインで200とLoginResponseが返される")
    void login_validUsername_returns200() throws Exception {
        var response = new LoginResponse(1L, "tanaka", "田中花子", Role.EMPLOYEE);
        when(authService.login("tanaka")).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tanaka\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("tanaka"))
                .andExpect(jsonPath("$.name").value("田中花子"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 未登録ユーザーで401が返される")
    void login_unknownUsername_returns401() throws Exception {
        when(authService.login("unknown")).thenThrow(new EmployeeNotFoundException("unknown"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"unknown\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("EMPLOYEE_NOT_FOUND"));
    }
}
