package com.hrth.ustock.exception.domain.stock;

import com.hrth.ustock.exception.common.CustomExceptionType;
import org.springframework.http.HttpStatus;

public enum StockExceptionType implements CustomExceptionType {
    STOCK_NOT_FOUND("해당 종목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    STOCK_CANNOT_PURCHASE("해당 금액으로는 구매할 수 없습니다.", HttpStatus.BAD_REQUEST),
    STOCK_NOT_PUBLIC("해당 종목이 상장되지 않은 날짜입니다.", HttpStatus.BAD_REQUEST),
    CURRENT_NOT_FOUND("현재가 정보를 조회하는데 실패하였습니다.", HttpStatus.NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus status;

    StockExceptionType(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    @Override
    public String message() {
        return "";
    }

    @Override
    public HttpStatus status() {
        return null;
    }
}
