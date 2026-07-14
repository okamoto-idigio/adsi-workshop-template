package com.example.attendance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.dto.LoginResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.Role;
import com.example.attendance.exception.EmployeeNotFoundException;
import com.example.attendance.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("登録済みユーザー名でログインするとLoginResponseが返される")
    void login_existingUser_returnsLoginResponse() {
        var employee = Employee.builder()
                .id(1L)
                .username("tanaka")
                .name("田中花子")
                .role(Role.EMPLOYEE)
                .build();
        when(employeeRepository.findByUsername("tanaka")).thenReturn(Optional.of(employee));

        LoginResponse result = authService.login("tanaka");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("tanaka");
        assertThat(result.name()).isEqualTo("田中花子");
        assertThat(result.role()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    @DisplayName("未登録のユーザー名でログインするとEmployeeNotFoundExceptionが投げられる")
    void login_unknownUser_throwsEmployeeNotFoundException() {
        when(employeeRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("unknown"))
                .isInstanceOf(EmployeeNotFoundException.class);
    }
}
