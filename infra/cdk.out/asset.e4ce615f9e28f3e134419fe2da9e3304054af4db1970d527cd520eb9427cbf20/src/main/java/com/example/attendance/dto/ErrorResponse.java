package com.example.attendance.dto;

public record ErrorResponse(
        String message,
        String code
) {
}
