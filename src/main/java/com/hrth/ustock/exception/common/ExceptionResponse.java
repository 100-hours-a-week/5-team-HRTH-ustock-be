package com.hrth.ustock.exception.common;

public record ExceptionResponse(
    int status, String message
) { }
