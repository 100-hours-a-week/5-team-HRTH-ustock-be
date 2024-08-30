package com.hrth.ustock.exception.domain.chart;

import com.hrth.ustock.exception.common.CustomException;
import com.hrth.ustock.exception.common.CustomExceptionType;

public class ChartException extends CustomException {

    public ChartException(CustomExceptionType customExceptionType) {
        super(customExceptionType);
    }
}
