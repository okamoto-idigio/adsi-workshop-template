package com.example.attendance.controller;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.attendance.config.SessionUser;
import com.example.attendance.dto.EmployeeSummary;
import com.example.attendance.entity.Role;
import com.example.attendance.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    private MockHttpSession createSession(Long id, Role role) {
        var session = new MockHttpSession();
        session.setAttribute(SessionUser.SESSION_KEY, new SessionUser(id, "testuser", "テストユーザー", role));
        return session;
    }

    @Test
    @DisplayName("GET /api/employees: ADMINは200と社員一覧を返す")
    void getEmployees_admin_returns200() throws Exception {
        // Arrange
        var session = createSession(1L, Role.ADMIN);
        var employees = List.of(
                new EmployeeSummary(1L, "管理者"),
                new EmployeeSummary(2L, "田中太郎")
        );
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // Act & Assert
        mockMvc.perform(get("/api/employees").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("管理者"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("田中太郎"));
    }

    @Test
    @DisplayName("GET /api/employees: EMPLOYEEは403を返す")
    void getEmployees_employee_returns403() throws Exception {
        var session = createSession(2L, Role.EMPLOYEE);

        mockMvc.perform(get("/api/employees").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/employees: 未認証は401を返す")
    void getEmployees_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }
}
