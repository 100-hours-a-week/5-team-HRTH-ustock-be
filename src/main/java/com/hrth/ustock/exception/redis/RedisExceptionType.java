package com.hrth.ustock.exception.redis;

import com.hrth.ustock.exception.common.CustomExceptionType;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum RedisExceptionType implements CustomExceptionType {
    SERIALIZE_FAILED("Object->JSON 직렬화에 실패했습니다", INTERNAL_SERVER_ERROR),
    DESERIALIZE_FAILED("JSON->Object 역직렬화에 실패했습니다", INTERNAL_SERVER_ERROR),
    VALUE_NOT_FOUND("Redis 조회에 실패했습니다", NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus status;

    RedisExceptionType(String message, HttpStatus status) {
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
