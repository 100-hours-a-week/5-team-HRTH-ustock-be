package com.hrth.ustock.exception.domain.user;

import com.hrth.ustock.exception.common.CustomException;
import com.hrth.ustock.exception.common.CustomExceptionType;

public class UserException extends CustomException {

    public UserException(CustomExceptionType customExceptionType) {
        super(customExceptionType);
    }
}
