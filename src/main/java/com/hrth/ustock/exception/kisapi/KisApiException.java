package com.hrth.ustock.exception.kisapi;

import com.hrth.ustock.exception.common.CustomException;
import com.hrth.ustock.exception.common.CustomExceptionType;

public class KisApiException extends CustomException {
    public KisApiException(CustomExceptionType customExceptionType) {
        super(customExceptionType);
    }
}
