package com.hrth.ustock.exception.domain.portfolio;

import com.hrth.ustock.exception.common.CustomExceptionType;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum PortfolioExceptionType implements CustomExceptionType {
    PORTFOLIO_NOT_FOUND("해당 포트폴리오를 찾을 수 없습니다.", NOT_FOUND),
    PORTFOLIO_ALREADY_EXIST("이미 존재하는 포트폴리오 이름입니다.", CONFLICT),
    HOLDING_NOT_FOUND("보유 종목 정보를 찾을 수 없습니다.", NOT_FOUND),
    HOLDING_INPUT_INVALID("입력값이 범위를 초과하였습니다.", BAD_REQUEST),
    ;

    private final String message;
    private final HttpStatus status;

    PortfolioExceptionType(String message, HttpStatus status) {
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
