package com.hrth.ustock.exception.kisApi;

import com.hrth.ustock.exception.common.CustomExceptionType;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

public enum KisApiExceptionType implements CustomExceptionType {
    API_REQUEST_FAILED("API 요청에 실패했습니다", SERVICE_UNAVAILABLE),
    ;

    private final String message;
    private final HttpStatus status;

    KisApiExceptionType(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }
}
