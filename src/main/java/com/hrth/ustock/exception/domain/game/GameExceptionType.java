package com.hrth.ustock.exception.domain.game;

import com.hrth.ustock.exception.common.CustomExceptionType;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum GameExceptionType implements CustomExceptionType {
    INVALID_YEAR_INPUT("올바르지 않은 년도 값입니다.", BAD_REQUEST),
    GAME_NOT_FOUND("게임 정보를 조회할 수 없습니다.", NOT_FOUND),
    STOCK_NOT_FOUND("종목 정보를 조회할 수 없습니다.", NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus status;

    GameExceptionType(String message, HttpStatus status) {
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
