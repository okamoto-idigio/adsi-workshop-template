package com.example.attendance.dto;

import com.example.attendance.entity.Role;

public record LoginResponse(
        Long id,
        String username,
        String name,
        Role role
) {
}
