package br.gov.mt.seletivo_seplag.api;


import java.time.OffsetDateTime;

public record ApiError(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}
