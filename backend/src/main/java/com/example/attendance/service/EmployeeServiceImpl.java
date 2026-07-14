package com.example.attendance.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.attendance.dto.EmployeeSummary;
import com.example.attendance.repository.EmployeeRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<EmployeeSummary> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(emp -> new EmployeeSummary(emp.getId(), emp.getName()))
                .toList();
    }
}
