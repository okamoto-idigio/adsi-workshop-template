package com.example.attendance.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.attendance.entity.Employee;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("存在するユーザー名で検索すると社員が返される")
    void findByUsername_existing_returnsEmployee() {
        Optional<Employee> result = employeeRepository.findByUsername("tanaka");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("田中花子");
    }

    @Test
    @DisplayName("存在しないユーザー名で検索するとemptyが返される")
    void findByUsername_notExisting_returnsEmpty() {
        Optional<Employee> result = employeeRepository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }
}
