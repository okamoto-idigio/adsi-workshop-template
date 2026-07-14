package com.example.attendance.service;

import com.example.attendance.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(String username);
}
