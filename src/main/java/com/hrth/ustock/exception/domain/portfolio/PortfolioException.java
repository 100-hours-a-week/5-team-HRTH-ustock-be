package com.hrth.ustock.exception.domain.portfolio;

import com.hrth.ustock.exception.common.CustomException;
import com.hrth.ustock.exception.common.CustomExceptionType;

public class PortfolioException extends CustomException {

    public PortfolioException(CustomExceptionType customExceptionType) {
        super(customExceptionType);
    }
}
