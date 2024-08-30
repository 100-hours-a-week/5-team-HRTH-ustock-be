package com.hrth.ustock.exception.domain.chart;

import com.hrth.ustock.exception.common.CustomExceptionType;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum ChartExceptionType implements CustomExceptionType {
    CHART_NOT_FOUND("차트 정보를 찾지 못하였습니다.", NOT_FOUND),
    PERIOD_NOT_ALLOWED("올바르지 않은 조회 기간입니다.", BAD_REQUEST)
    ;

    private final String message;
    private final HttpStatus status;

    ChartExceptionType(String message, HttpStatus status) {
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
