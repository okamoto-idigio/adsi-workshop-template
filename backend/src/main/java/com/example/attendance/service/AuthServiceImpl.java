package com.example.attendance.service;

import org.springframework.stereotype.Service;

import com.example.attendance.dto.LoginResponse;
import com.example.attendance.entity.Employee;
import com.example.attendance.exception.EmployeeNotFoundException;
import com.example.attendance.repository.EmployeeRepository;

@Service
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;

    public AuthServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public LoginResponse login(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new EmployeeNotFoundException(username));

        return new LoginResponse(
                employee.getId(),
                employee.getUsername(),
                employee.getName(),
                employee.getRole()
        );
    }
}
