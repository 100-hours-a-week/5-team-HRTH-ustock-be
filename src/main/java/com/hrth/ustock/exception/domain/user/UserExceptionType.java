package com.hrth.ustock.exception.domain.user;

import com.hrth.ustock.exception.common.CustomExceptionType;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum UserExceptionType implements CustomExceptionType {
    USER_NOT_FOUND("해당 사용자를 찾을 수 없습니다.", NOT_FOUND),
    REFRESH_NOT_VALID("Refresh 토큰이 유효하지 않습니다", UNAUTHORIZED),
    ;

    private final String message;
    private final HttpStatus status;

    UserExceptionType(String message, HttpStatus status) {
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
