package com.flamedavid.eurovision.dtos;

public record ErrorResponseDTO (
    String message,
    int statusCode
) {}
