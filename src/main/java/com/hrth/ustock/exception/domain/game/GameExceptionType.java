package com.hrth.ustock.exception.domain.game;

import com.hrth.ustock.exception.common.CustomExceptionType;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public enum GameExceptionType implements CustomExceptionType {
    INVALID_YEAR_INPUT("올바르지 않은 년도 값입니다.", BAD_REQUEST),
    ACT_NOT_VALID("enum값은 BUY, SELL 둘 중 하나여야 합니다.", BAD_REQUEST),
    LEVEL_NOT_VALID("enum값은 ONE, TWO, THREE 셋 중 하나여야 합니다.", BAD_REQUEST),
    GAME_NOT_FOUND("게임 정보를 조회할 수 없습니다.", NOT_FOUND),
    STOCK_NOT_FOUND("종목 정보를 조회할 수 없습니다.", NOT_FOUND),
    YEAR_INFO_NOT_FOUND("종목의 연도 정보를 조회할 수 없습니다.", NOT_FOUND),
    HINT_NOT_FOUND("종목 힌트를 조회할 수 없습니다.", NOT_FOUND),
    NO_HOLDING_STOCK("종목을 보유하고 있지 않습니다.", BAD_REQUEST),
    NOT_ENOUGH_BUDGET("잔액이 부족합니다.", BAD_REQUEST),
    NOT_ENOUGH_QUANTITY("수량이 부족합니다.", BAD_REQUEST);

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
