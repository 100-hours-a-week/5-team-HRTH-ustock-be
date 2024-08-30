package com.hrth.ustock.exception.common;

import org.springframework.http.HttpStatus;

public interface CustomExceptionType {
    String message();
    HttpStatus status();
}
