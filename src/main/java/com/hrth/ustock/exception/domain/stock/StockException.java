package com.hrth.ustock.exception.domain.stock;

import com.hrth.ustock.exception.common.CustomException;
import com.hrth.ustock.exception.common.CustomExceptionType;

public class StockException extends CustomException {

    public StockException(CustomExceptionType customExceptionType) {
        super(customExceptionType);
    }
}
