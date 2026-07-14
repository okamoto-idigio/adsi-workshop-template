package com.example.attendance.exception;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(String username) {
        super("ユーザーが見つかりません: " + username);
    }
}
