package com.hrth.ustock.exception.domain.chart;

import com.hrth.ustock.exception.common.CustomExceptionType;
import org.springframework.http.HttpStatus;

public enum ChartExceptionType implements CustomExceptionType {
    CHART_NOT_FOUND("차트 정보를 찾지 못하였습니다.", HttpStatus.NOT_FOUND),
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
