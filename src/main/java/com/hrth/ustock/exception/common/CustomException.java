package com.hrth.ustock.exception.common;

public class CustomException extends RuntimeException {

    private final CustomExceptionType customExceptionType;

    public CustomException(CustomExceptionType customExceptionType) {
        super(customExceptionType.message());
        this.customExceptionType = customExceptionType;
    }

    public CustomExceptionType getExceptionType() {
        return customExceptionType;
    }
}
