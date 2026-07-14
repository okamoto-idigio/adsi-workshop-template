package com.example.attendance.config;

import com.example.attendance.entity.Role;

public record SessionUser(
        Long id,
        String username,
        String name,
        Role role
) {
    public static final String SESSION_KEY = "loginUser";
}
